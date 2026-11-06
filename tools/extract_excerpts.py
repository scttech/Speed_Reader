"""Cut excerpt texts from project_assets/source_texts using excerpt_specs.SPECS.

Usage: python3 extract_excerpts.py <out_dir>   -> writes <id>.txt (cleaned text) per spec
"""
import re, sys, os
sys.path.insert(0, os.path.dirname(__file__))
from excerpt_specs import SPECS

ROOT = os.path.join(os.path.dirname(__file__), "..", "project_assets", "source_texts")

def paragraphs(path):
    raw = open(path, encoding="utf-8").read()
    raw = re.sub(r"\[Illustration[^\]]*\]\]?", "", raw)        # book-plate stubs, incl. captions and copyright lines
    lines = raw.split("\n")
    paras, cur, start = [], [], None
    for i, ln in enumerate(lines, 1):
        if ln.strip() == "":
            if cur: paras.append((start, " ".join(cur))); cur, start = [], None
        else:
            if start is None: start = i
            cur.append(ln.strip())
    if cur: paras.append((start, " ".join(cur)))
    return paras

def is_footnote(t):
    return bool(re.match(r"^(\[\d+\]|\[\*\]|[\u00b9\u00b2\u00b3\u2070-\u2079]|\*)\s", t))

def clean(t):
    t = re.sub(r"\[Illustration[^\]]*\]", "", t)
    t = re.sub(r"\s*\[(?:\d+(?::\d+)?|\*)\]", "", t)                       # footnote markers like [2]
    t = re.sub(r"\s*[\u00b9\u00b2\u00b3\u2070-\u2079]", "", t)
    t = t.replace("_", "")
    return re.sub(r"\s+", " ", t).strip()

def words(t): return len(t.split())

# Sources whose paragraphs begin with verse / section numbers ("1. ", "IV. ")
STRIP_NUM = ("art-of-war", "meditations")

def strip_number(t):
    t = re.sub(r"^(\d+(,\s*\d+)*|[IVXL]+)\.\s+", "", t)
    return re.sub(r"^\d+\s+(?=[A-Z])", "", t)               # stray bare numbers ("9 There are not more...")

# Sources whose bracketed paragraphs are a translator's commentary, not the work itself
DROP_BRACKETED = ("art-of-war", "montaigne")

# Montaigne (Cotton's translation) is full of untranslated Latin verse and bracketed source notes.
ENGLISH_HINTS = {"the","of","and","to","in","is","that","it","for","as","with","his","by","be","not","he","a","i","this","are","was","we","which","or","but","have","from","at","on"}
def looks_latin(t):
    w = re.findall(r"[A-Za-z]+", t.lower())
    return 0 < len(w) <= 45 and sum(x in ENGLISH_HINTS for x in w) / len(w) < 0.12

def extract(spec, ended_at):
    sid, f, start, target = spec[0], spec[1], spec[2], spec[3]
    paras = paragraphs(os.path.join(ROOT, f + ".txt"))
    if isinstance(start, int):
        idx = next(i for i, (ln, _) in enumerate(paras) if ln >= start)
    elif isinstance(start, str):
        idx = next(i for i, (_, p) in enumerate(paras) if start in p)
    else:
        idx = ended_at[start[1]] + 1
    while words(paras[idx][1]) < 12 or paras[idx][1].isupper(): idx += 1   # skip headings / epigraph fragments
    out, n, j = [], 0, idx
    while n < target and j < len(paras):
        if any(k in f for k in DROP_BRACKETED) and paras[j][1].lstrip().startswith("["):
            j += 1; continue
        if re.match(r"^-{3,}$", paras[j][1].strip()):              # horizontal rules between sections
            j += 1; continue
        if "art-of-war" in f and re.match(r"^[\"\u201c\u2018].{0,120}[\"\u201d\u2019]$", paras[j][1].strip()):
            j += 1; continue                                       # quoted verse fragments from the commentary
        if "montaigne" in f and (looks_latin(paras[j][1]) or re.search(r"--\s*(Horace|Virgil|Lucretius|Juvenal|Ovid|Seneca|Cicero|Lucan|Persius|Catullus|Martial|Tibullus)", paras[j][1])):
            j += 1; continue
        if is_footnote(paras[j][1]) or re.match(r"^\d\d [A-Z][^.!?]{0,40}$", paras[j][1]):
            j += 1; continue                                  # drop footnote paragraphs
        p = clean(paras[j][1])
        if any(k in f for k in STRIP_NUM): p = strip_number(p)
        if "montaigne" in f: p = re.sub(r"\s*-*\[[^\]]*\]-*", " ", p)          # inline bracketed notes
        if "meditations" in f: p = p.replace("every tree md plant", "every tree and plant").replace("belong unto thin?", "belong unto thee?")
        if "meditations" in f: p = re.sub(r"(?<=[.?!:;])\s+[IVXL]{1,5}\.\s+(?=[A-Z])", " ", p)   # stray section numerals
        if re.match(r"^(CHAPTER|Chapter|STAVE|PART) [IVXLC0-9]+", p):
            if n >= 0.85 * target: break          # never run into the next chapter...
            j += 1; continue                       # ...unless the excerpt is still too short; then step over the heading
        if n + words(p) > target + 350:                 # very long paragraph: stop at a sentence boundary
            kept = []
            for sent in re.split(r"(?<=[.!?])\s+(?=[A-Z\u2018\u201c\"'])", p):
                kept.append(sent); n += words(sent)
                if n >= target: break
            out.append(" ".join(kept)); break
        out.append(p); n += words(p); j += 1
    return "\n\n".join(out), j - 1

if __name__ == "__main__":
    out_dir = sys.argv[1]; os.makedirs(out_dir, exist_ok=True)
    ended = {}
    for spec in SPECS:
        text, end = extract(spec, ended); ended[spec[0]] = end
        open(os.path.join(out_dir, spec[0] + ".txt"), "w").write(text + "\n")
        print(spec[0], words(text), "words |", text[:60].replace("\n", " "))
