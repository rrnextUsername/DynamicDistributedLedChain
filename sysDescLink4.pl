context(ctxReg,"localhost",    "TCP", 8010).
context(ctxLed4,"localhost",    "TCP", 8060).

qactor(ledControl4, ctxLed4, "actors.ChainLinkActor").
qactor(ledActor_ledControl4, ctxLed4, "actors.MockLedActor").