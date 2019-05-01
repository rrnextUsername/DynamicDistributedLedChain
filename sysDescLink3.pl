context(ctxReg,"localhost",    "TCP", 8010).
context(ctxLed3,"localhost",    "TCP", 8040).

qactor(ledControl3, ctxLed3, "actors.DynamicChainLinkActor").

qactor(ledActor_ledControl3, ctxLed3, "actors.LedSegmentActor").
bind(ledControl3,ledActor_ledControl3).