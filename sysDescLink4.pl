context(ctxReg,"192.168.137.1",    "TCP", 8010).
context(ctxLed4,"192.168.137.2",    "TCP", 8100).

qactor(ledControl4, ctxLed4, "actors.ChainLinkActor").
qactor(ledActor_ledControl4, ctxLed4, "actors.MockLedActor").