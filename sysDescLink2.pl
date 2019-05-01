context(ctxReg,"localhost",    "TCP", 8010).
context(ctxLed2,"localhost",    "TCP", 8030).

qactor( ledControl2, ctxLed2, "actors.DynamicChainLinkActor").

qactor( ledActor_ledControl2, ctxLed2, "actors.LedSegmentActor").
bind(ledControl2,ledActor_ledControl2).