package actors

import it.unibo.kactor.ActorBasic
import it.unibo.kactor.ApplMessage
import kotlinx.coroutines.CoroutineScope
import stateMachine.QAKcmds
import stateMachine.TransitionTable

class MockLedActor(name: String, scope: CoroutineScope) : ActorBasic(name, scope) {
    enum class States {
        ON,
        OFF;
    }

    private var state = States.OFF
    private val transitionTable = TransitionTable<States, String>()


    init {
        state = States.OFF
        transitionTableSetup()
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