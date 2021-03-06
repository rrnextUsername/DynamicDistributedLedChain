package appl

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import stateMachine.QAKcmds

fun main() = runBlocking {
    QakContext.createContexts("localhost", this, "sysDescLink3.pl", "sysRules.pl")
    delay(15000)
    MsgUtil.sendMsg(QAKcmds.ControlRemoveFromRegistry.id, "remove", QakContext.getActor("ledControl3")!!)
    delay(5000)
    MsgUtil.sendMsg(QAKcmds.ControlAddToRegistry.id, "remove", QakContext.getActor("ledControl3")!!)
}