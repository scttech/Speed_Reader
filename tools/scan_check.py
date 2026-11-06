import sys, re; sys.path.insert(0,"tools")
import build_excerpts as b, extract_excerpts as ex
from scan_questions import SCAN_NEW
ended={}; texts={}
for spec in b.SPECS:
    t,e=ex.extract(spec,ended); ended[spec[0]]=e; texts[spec[0]]=t
bad=0
for sid,items in SCAN_NEW.items():
    low=texts[sid].lower()
    for q,c,ws,ev in items:
        if ev not in texts[sid]: print("EVIDENCE MISSING",sid,repr(ev)); bad+=1
        for w in ws:
            if re.search(r"(?<![a-z0-9])"+re.escape(w.lower())+r"(?![a-z0-9])", low): print("WRONG OPTION IN TEXT",sid,q[:40],"|",w); bad+=1
        if len({c,*ws})<4: print("DUPLICATE OPTIONS",sid,q[:40]); bad+=1
print("checked",sum(len(v) for v in SCAN_NEW.values()),"questions in",len(SCAN_NEW),"excerpts; problems:",bad)
