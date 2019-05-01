%%static context
context(ctxReg,"localhost",    "TCP", 8000).
context(ctxLed1,"localhost",    "TCP", 8010).

qactor( ledControl1, ctxLed1, "actors.StaticChainLinkActor").
qactor( ledControl4, ctxLed1, "actors.StaticChainLinkActor").


qactor( ledActor_ledControl1, ctxLed1, "actors.LedSegmentActor").
bind(ledControl1,ledActor_ledControl1).
qactor( ledActor_ledControl4, ctxLed1, "actors.LedSegmentActor").
bind(ledControl4,ledActor_ledControl4).