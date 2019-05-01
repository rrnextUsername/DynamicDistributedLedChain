package appl

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext
import kotlinx.coroutines.runBlocking
import stateMachine.QAKcmds

fun main() = runBlocking {
    QakContext.createContexts("192.168.137.2", this, "sysDescLink4.pl", "sysRules.pl")
    /*readLine()
    MsgUtil.sendMsg(QAKcmds.ControlRemoveFromRegistry.id, "remove", QakContext.getActor("ledControl4")!!)*/
}