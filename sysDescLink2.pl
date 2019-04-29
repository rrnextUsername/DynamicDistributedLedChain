context(ctxReg,"192.168.137.1",    "TCP", 8010).
context(ctxLed2,"192.168.137.1",    "TCP", 8030).

qactor( ledControl2, ctxLed2, "actors.ChainLinkActor").
qactor( ledActor_ledControl2, ctxLed2, "actors.LedSegmentActor").