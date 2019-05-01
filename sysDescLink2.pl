%%static context
context(ctxReg,"localhost",    "TCP", 8000).
context(ctxLed1,"localhost",    "TCP", 8010).

%%dynamic context
context(ctxLed2,"localhost",    "TCP", 8020).
qactor( ledControl2, ctxLed2, "actors.DynamicChainLinkActor").

qactor( ledActor_ledControl2, ctxLed2, "actors.LedSegmentActor").
bind(ledControl2,ledActor_ledControl2).