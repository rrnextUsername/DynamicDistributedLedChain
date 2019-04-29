context(ctxReg,"localhost",    "TCP", 8010).
context(ctxLed1,"localhost",    "TCP", 8020).

qactor( ledControl1, ctxLed1, "actors.ChainLinkActor").
qactor( ledActor_ledControl1, ctxLed1, "actors.LedSegmentActor").
qactor( ledControl4, ctxLed1, "actors.ChainLinkActor").
qactor( ledActor_ledControl4, ctxLed1, "actors.LedSegmentActor").
