package actors

import kotlinx.coroutines.CoroutineScope
import stateMachine.QAKcmds

class DynamicChainLinkActor(name: String, scope: CoroutineScope) : StaticChainLinkActor(name, scope) {

    override fun transitionTableSetup() {
        super.transitionTableSetup()

        //dynamic chain
        transitionTable.putAction(States.LIVE_TOKEN, QAKcmds.ControlRemoveFromRegistry.id) {
            println("$name:: received remove_from_registry -> de-registering myself from the registry and passing token ahead.")

            forward(QAKcmds.ControlStop.id, "removing myself -> stop chain momentarily", next!!)
            doControlRemoveFromRegistry()
            forward(QAKcmds.ControlToken.id, "removing myself -> passing token ahead", next!!)
            forward(QAKcmds.ControlStart.id, "removing myself ->starting chain again", next!!)
        }


        transitionTable.putAction(States.SLEEP_TOKEN, QAKcmds.ControlRemoveFromRegistry.id) {
            println("$name:: received remove_from_registry -> de-registering myself from the registry and passing token ahead.")

            doControlRemoveFromRegistry()
            forward(QAKcmds.ControlToken.id, "removing myself -> passing token ahead", next!!)
        }


        transitionTable.putAction(States.LIVE, QAKcmds.ControlRemoveFromRegistry.id) {
            println("$name:: received remove_from_registry -> de-registering myself from the registry.")

            forward(QAKcmds.ControlStop.id, "removing myself -> stop chain momentarily", next!!)
            doControlRemoveFromRegistry()
            forward(QAKcmds.ControlStart.id, "removing myself ->starting chain again", next!!)
        }


        transitionTable.putAction(States.SLEEP, QAKcmds.ControlRemoveFromRegistry.id) {
            println("$name:: received remove_from_registry -> de-registering myself from the registry.")

            doControlRemoveFromRegistry()
        }
    }

    private suspend fun doControlRemoveFromRegistry() {
        state = States.INIT

        val msg = QAKcmds.RegistryRemoveLink("$name|$ctx|$act")
        emit(msg.id, msg.cmd)

        turnOffLed()
    }
}