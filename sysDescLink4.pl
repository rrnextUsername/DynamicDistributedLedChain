%%static context
context(ctxReg,"localhost",    "TCP", 8000).
context(ctxLed1,"localhost",    "TCP", 8010).

%%dynamic context
context(ctxLed4,"localhost",    "TCP", 8040).
qactor(ledControl5, ctxLed4, "actors.DynamicChainLinkActor").

qactor(ledActor_ledControl5, ctxLed4, "actors.MockLedActor").
bind(ledControl5,ledActor_ledControl5).