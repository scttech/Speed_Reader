"""Which comprehension questions are suitable for Skim & scan.

A question is "scannable" when its answer is a specific, findable fact (a name, number, place, object,
short quotation) that distinctive words in the question lead you to. Everything NOT listed here is
scannable. Questions about reasons, characterisation, argument or inference are listed below.
"""
NOT_SCANNABLE = {
 "p03.0","p04.4","p05.4","p06.0","p06.1","p10.1","p10.4","p11.2","p13.1","p14.4","p15.3","p16.1","p16.4",
 "p18.0","p18.1","p18.3","p18.4","p19.1","p19.2","p20.4","p21.3","p22.1","p22.4","p23.4","p25.1","p26.1",
 "p27.0","p28.3","p28.4","p30.0","p30.4","p32.0","p33.3",
 "p34.3","p35.1","p35.4","p36.1","p36.2","p36.3","p37.1","p37.2","p37.4","p38.0","p38.1","p38.2","p38.3",
 "p39.0","p39.1","p40.0","p40.2","p40.3","p40.4","p41.4","p42.1","p42.2","p42.4","p43.0","p44.3","p45.2",
 "p45.3","p45.4","p46.0","p46.1","p46.3","p46.4","p48.0","p48.4","p49.0","p49.1","p50.0","p50.2","p50.3",
 "p52.0","p52.1","p52.2","p53.2","p53.4","p54.2","p54.3","p54.4","p56.0","p56.3","p57.0","p57.1","p58.1",
 "p59.2","p60.1","p60.3","p61.1","p61.3","p62.2","p62.3","p62.4","p63.0","p63.1","p63.4","p65.4","p66.1",
 "p67.4",
}

# Manual evidence where the automatic match is wrong: (id, question index) -> a short exact fragment of the
# sentence that holds the answer. The build expands it to the full sentence.
EVIDENCE_FIX = {
    ("p09", 0): "the year of grace 1751",
    ("p43", 2): "His father, Elmo",
    ("p45", 1): "The lad fled on the instant",
    ("p58", 2): "has been crying night and day for some sort of medicine",
}
