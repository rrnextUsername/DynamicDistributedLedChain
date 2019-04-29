package appl

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext
import kotlinx.coroutines.runBlocking
import stateMachine.QAKcmds

fun main() = runBlocking {
    println("versione 1.0")
    QakContext.createContexts("192.168.137.1", this, "sysDescLink4.pl", "sysRules.pl")
}