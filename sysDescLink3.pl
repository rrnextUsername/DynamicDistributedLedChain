context(ctxReg,"192.168.137.1",    "TCP", 8010).
context(ctxLed3,"192.168.137.1",    "TCP", 8040).

qactor(ledControl3, ctxLed3, "actors.ChainLinkActor").
qactor(ledActor_ledControl3, ctxLed3, "actors.LedSegmentActor").