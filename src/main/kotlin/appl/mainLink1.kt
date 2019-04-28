package appl

import it.unibo.kactor.QakContext
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    QakContext.createContexts("localhost", this, "sysDescLink1.pl", "sysRules.pl")
}