"""Build app/src/main/assets/excerpts/*.json from the source texts + questions.

Usage (from repo root):  python3 tools/build_excerpts.py
"""
import json, os, random, sys
sys.path.insert(0, os.path.dirname(__file__))
from excerpt_specs import SPECS
from excerpt_questions import QUESTIONS
from option_overrides import OVERRIDES, PADS
from scan_tags import NOT_SCANNABLE, EVIDENCE_FIX
from scan_questions import SCAN_NEW
from scan_support import best_sentence, expand
import extract_excerpts as ex

OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets", "excerpts")

def rebalance(questions_by_id):
    """Deterministically move each correct answer so all four positions are used ~equally."""
    total = sum(len(v) for v in questions_by_id.values())
    targets = ([0, 1, 2, 3] * (total // 4 + 1))[:total]
    random.Random(2026).shuffle(targets)
    out, k = {}, 0
    for sid, qs in questions_by_id.items():
        new = []
        for q, opts, ans in qs:
            opts = list(opts); t = targets[k]; k += 1
            opts[ans], opts[t] = opts[t], opts[ans]      # swap correct option into target slot
            new.append((q, opts, t))
        out[sid] = new
    return out

def build_scan(sid, text, comprehension_qs):
    """Skim & scan questions for one excerpt: the scannable comprehension questions plus the new fact-hunts,
    each with the sentence that holds its answer (shown to the reader afterwards)."""
    out = []
    for i, (q, opts, ans) in enumerate(comprehension_qs):
        if f"{sid}.{i}" in NOT_SCANNABLE:
            continue
        if (sid, i) in EVIDENCE_FIX:
            ev = expand(text, EVIDENCE_FIX[(sid, i)])
        else:
            ev, conf = best_sentence(text, q, opts[ans])
            assert ev and ev in text, f"{sid}.{i}: no evidence sentence"
        out.append((q, list(opts), ans, ev))
    for q, correct, wrong, fragment in SCAN_NEW.get(sid, []):
        out.append((q, [correct] + list(wrong), 0, expand(text, fragment)))
    return out

def rebalance_scan(items_by_id):
    """Same idea as rebalance(): spread the correct answer evenly over positions A-D, deterministically."""
    total = sum(len(v) for v in items_by_id.values())
    targets = ([0, 1, 2, 3] * (total // 4 + 1))[:total]
    random.Random(2027).shuffle(targets)
    out, k = {}, 0
    for sid, items in items_by_id.items():
        new = []
        for q, opts, ans, ev in items:
            opts = list(opts); t = targets[k]; k += 1
            opts[ans], opts[t] = opts[t], opts[ans]
            new.append((q, opts, t, ev))
        out[sid] = new
    return out

def apply_overrides():
    """Swap in rewritten options (correct option listed first in each override)."""
    for (sid, idx), opts in OVERRIDES.items():
        q = QUESTIONS[sid][idx][0]
        QUESTIONS[sid][idx] = (q, list(opts), 0)
    for (sid, idx), text in PADS.items():
        q, opts, ans = QUESTIONS[sid][idx]
        opts = list(opts)
        wrong = [j for j in range(4) if j != ans]
        longest = max(wrong, key=lambda j: len(opts[j]))
        assert len(text) > len(opts[ans]), f"{sid}.{idx}: pad not longer than the correct option"
        opts[longest] = text
        QUESTIONS[sid][idx] = (q, opts, ans)

def main():
    apply_overrides()
    strict = "--strict" in sys.argv
    missing = [spec[0] for spec in SPECS if spec[0] not in QUESTIONS]
    if missing and strict:
        sys.exit(f"missing questions for: {missing}")
    if missing:
        print(f"WARNING: skipping {len(missing)} excerpts without questions yet: {missing[0]}..{missing[-1]}")
    ready = [spec for spec in SPECS if spec[0] in QUESTIONS]
    # validate everything before touching the output folder
    for spec in ready:
        qs = QUESTIONS[spec[0]]
        assert len(qs) == 5, f"{spec[0]}: need 5 questions"
        for q, opts, ans in qs:
            assert len(opts) == 4 and len(set(opts)) == 4 and 0 <= ans < 4, f"{spec[0]}: bad question {q!r}"
    os.makedirs(OUT, exist_ok=True)
    for f in os.listdir(OUT):
        if f.endswith(".json"): os.remove(os.path.join(OUT, f))
    balanced = rebalance({spec[0]: QUESTIONS[spec[0]] for spec in ready})
    texts = {}; _ended = {}
    for spec in SPECS:
        texts[spec[0]], _ended[spec[0]] = ex.extract(spec, _ended)
    scan_balanced = rebalance_scan({spec[0]: build_scan(spec[0], texts[spec[0]], QUESTIONS[spec[0]]) for spec in ready})
    ended = {}
    for spec in SPECS:                     # walk ALL specs so "continue from previous" chains stay correct
        if spec[0] not in QUESTIONS:
            _, ended[spec[0]] = ex.extract(spec, ended)
            continue
        sid, _, _, _, kind, book, author, title = spec
        text, end = ex.extract(spec, ended); ended[sid] = end
        qs = balanced[sid]
        assert len(qs) == 5, f"{sid}: need 5 questions"
        for q, opts, ans in qs:
            assert len(opts) == 4 and len(set(opts)) == 4 and 0 <= ans < 4, f"{sid}: bad question {q!r}"
        doc = {"id": sid, "kind": kind, "bookTitle": book, "author": author, "title": title, "text": text,
               "questions": [{"text": q, "options": o, "correctIndex": a} for q, o, a in qs],
               "scan": [{"text": q, "options": o, "correctIndex": a, "evidence": ev} for q, o, a, ev in scan_balanced[sid]]}
        with open(os.path.join(OUT, sid + ".json"), "w", encoding="utf-8") as fh:
            json.dump(doc, fh, ensure_ascii=False, indent=1)
        print(sid, kind, ex.words(text), "words,", book, "-", title)

if __name__ == "__main__":
    main()
