context(ctxReg,"localhost",    "TCP", 8000).
context(ctxLed1,"localhost",    "TCP", 8010).

qactor(reg, ctxReg, "actors.RegistryActor").
qactor( button, ctxReg, "actors.ButtonGuiActor").
