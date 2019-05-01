%%static context
context(ctxReg,"localhost",    "TCP", 8000).
context(ctxLed1,"localhost",    "TCP", 8010).

%%dynamic context
context(ctxLed3,"localhost",    "TCP", 8030).
qactor(ledControl3, ctxLed3, "actors.DynamicChainLinkActor").

qactor(ledActor_ledControl3, ctxLed3, "actors.LedSegmentActor").
bind(ledControl3,ledActor_ledControl3).

