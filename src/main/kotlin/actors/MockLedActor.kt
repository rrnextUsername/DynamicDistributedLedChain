package actors

import it.unibo.kactor.ActorBasic
import it.unibo.kactor.ApplMessage
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import stateMachine.QAKcmds
import stateMachine.TransitionTable

class MockLedActor(name: String, scope: CoroutineScope) : ActorBasic(name, scope) {
    enum class States {
        ON,
        OFF;
    }

    private val ctxName = sysUtil.solve("qactor($name,CTX,_)", "CTX")!!
    private val hostAddr = sysUtil.solve("context($ctxName,ADDR,_,_)", "ADDR")!!
    private val hostProt = sysUtil.solve("context($ctxName,_,PROT,_)", "PROT")!!
    private val hostPort = sysUtil.solve("context($ctxName,_,_,PORT)", "PORT")!!
    private val actClass = "$javaClass"

    private val ctx = "context($ctxName,\"$hostAddr\",\"$hostProt\",$hostPort)."
    private val act = "qactor($name,$ctxName,\"$actClass\")."

    private var state = States.OFF
    private val transitionTable = TransitionTable<States, String>()


    init {
        state = States.OFF

        transitionTableSetup()
        registerOnControl()
    }

    private fun registerOnControl() {
        val control = sysUtil.solve("bind(CONTROL,$name)", "CONTROL")!!

        scope.launch { forward(QAKcmds.ControlAddLed.id, "$name|$ctx|$act", control) }
    }

    private fun transitionTableSetup() {
        transitionTable.putAction(States.ON, QAKcmds.LedOff.id) {
            println("$name:: received off -> turning led off.")
            ledOff()
        }
        transitionTable.putAction(States.OFF, QAKcmds.LedOn.id) {
            println("$name:: received on -> turning led on.")
            ledOn()
        }
    }

    override suspend fun actorBody(msg: ApplMessage) {
        print("$name: received $msg | state: $state ")
        transitionTable.action(state, msg.msgId())?.invoke() ?: println(" :: message not recognized -> dropping.")
    }

    private fun ledOff() {
        state = States.OFF

        println("LED -> OFF")
    }

    private fun ledOn() {
        state = States.ON


        println("LED -> ON")
    }
}