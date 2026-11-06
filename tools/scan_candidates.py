"""Print candidate fact sentences per excerpt for writing new scan questions: python3 scan_candidates.py p01 p17"""
import sys, re, json
sys.path.insert(0, "tools")
import build_excerpts as b, extract_excerpts as ex
from excerpt_questions import QUESTIONS
from scan_tags import NOT_SCANNABLE
from scan_support import sentences, best_sentence

b.apply_overrides()
ended = {}; texts = {}
for spec in b.SPECS:
    t, e = ex.extract(spec, ended); ended[spec[0]] = e; texts[spec[0]] = t

def score(s):
    w = s.split()
    if not 8 <= len(w) <= 40: return -1
    digits = 2 if re.search(r"\d", s) else 0
    names = len([x for x in w[1:] if re.match(r"[A-Z][a-z]{2,}", x)])
    return digits + min(names, 3) + (1 if any(c in s for c in "“\"") else 0)

if __name__ == "__main__":
    lo, hi = sys.argv[1], sys.argv[2]
    for sid in sorted(texts):
        if not (lo <= sid <= hi) or sid not in QUESTIONS: continue
        keep = [i for i in range(5) if f"{sid}.{i}" not in NOT_SCANNABLE]
        used = {best_sentence(texts[sid], QUESTIONS[sid][i][0], QUESTIONS[sid][i][1][QUESTIONS[sid][i][2]])[0] for i in keep}
        need = max(2, 4 - len(keep))
        cands = sorted([s for s in sentences(texts[sid]) if s not in used], key=score, reverse=True)[:need + 3]
        print(f"\n## {sid} need {need}")
        for k, s in enumerate(cands): print(f"  {k}. {s[:240]}")
