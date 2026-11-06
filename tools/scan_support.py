"""Helpers to find the sentence in an excerpt that holds the answer to a question (for the answer reveal)."""
import re

STOP = set("""about above after again against also because been before being between both could did does doing during each from
had has have having here himself herself into its itself just more most much must only other over own same should such than that the
their them then there these they this those through under until very was were what when where which while who whom why will with would
you your what which according does did say says how many much one two three four five from his her him she were are not was for and""".split())
GENERIC = set("what which according does did say says how many much who whom where when why one man his her".split())

ABBREVIATIONS = ["Mr.", "Mrs.", "Dr.", "St.", "Messrs.", "Col.", "Capt.", "M.R.C.S.", "M.D.", "No.", "Mme.", "A. M.", "P. M."]

def _guard(text):
    for i, ab in enumerate(ABBREVIATIONS):
        text = text.replace(ab, "\x01" + f"{i:02d}" + "\x02")
    return text

def _unguard(text):
    for i, ab in enumerate(ABBREVIATIONS):
        text = text.replace("\x01" + f"{i:02d}" + "\x02", ab)
    return text

def sentences(text):
    """Split into sentences without breaking after abbreviations like 'Mr.'; every result is an exact substring."""
    out = []
    for para in text.split("\n\n"):
        for part in re.split(r'(?<=[.!?”"])\s+(?=[A-Z“"‘\'(\[])', _guard(para)):
            part = _unguard(part).strip()
            if part:
                out.append(part)
    return out

def toks(s):
    return {w[:5] for w in re.findall(r"[a-z][a-z\-']+", s.lower().replace("’", "'")) if len(w) >= 4 and w not in STOP}

def best_sentence(text, question, correct):
    ans, qs = toks(correct), toks(question) - {t[:5] for t in GENERIC}
    best, best_score, conf = None, -1.0, 0.0
    for s in sentences(text):
        st = toks(s)
        a = len(ans & st) / len(ans) if ans else 0.0
        q = len(qs & st) / len(qs) if qs else 0.0
        score = 3 * a + q + 0.0001 * min(len(s), 200)
        if score > best_score:
            best, best_score, conf = s, score, (a if ans else q)
    return best, conf

def _spans(text):
    pos, out = 0, []
    for sent in sentences(text):
        i = text.index(sent, pos)
        out.append((i, i + len(sent)))
        pos = i + len(sent)
    return out

def expand(text, fragment):
    """The whole sentence(s) of `text` that the fragment touches (an exact substring of `text`)."""
    assert fragment in text, f"fragment not in text: {fragment!r}"
    i = text.index(fragment); j = i + len(fragment)
    touched = [(a, b) for a, b in _spans(text) if a < j and b > i]
    return text[touched[0][0]:touched[-1][1]]
