package appl

import it.unibo.kactor.QakContext
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    QakContext.createContexts("192.168.137.1", this, "sysDescLink1.pl", "sysRules.pl")
}