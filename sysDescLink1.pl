context(ctxReg,"192.168.137.1",    "TCP", 8010).
context(ctxLed1,"localhost",    "TCP", 8020).

qactor( ledControl1, ctxLed1, "actors.ChainLinkActor").
qactor( ledActor_ledControl1, ctxLed1, "actors.LedSegmentActor").
qactor( ledControl5, ctxLed1, "actors.ChainLinkActor").
qactor( ledActor_ledControl5, ctxLed1, "actors.LedSegmentActor").
