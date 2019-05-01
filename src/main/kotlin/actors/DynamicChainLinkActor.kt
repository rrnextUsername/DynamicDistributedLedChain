package actors

import kotlinx.coroutines.CoroutineScope
import stateMachine.QAKcmds

class DynamicChainLinkActor(name: String, scope: CoroutineScope) : StaticChainLinkActor(name, scope) {

    enum class DynamicStates : StateInterface {
        DEACTIVATED
    }

    override fun transitionTableSetup() {
        super.transitionTableSetup()

        transitionTable.putAction(StaticStates.LIVE_TOKEN, QAKcmds.ControlRemoveFromRegistry.id) {
            println("$name:: received remove_from_registry -> de-registering myself from the registry and passing token ahead.")

            forward(QAKcmds.ControlToken.id, "removing myself -> passing token ahead", next!!)
            doControlRemoveFromRegistry()
        }

        transitionTable.putAction(StaticStates.SLEEP_TOKEN, QAKcmds.ControlRemoveFromRegistry.id) {
            println("$name:: received remove_from_registry -> de-registering myself from the registry and passing token ahead.")

            forward(QAKcmds.ControlToken.id, "removing myself -> passing token ahead", next!!)

            doControlRemoveFromRegistry()
        }


        transitionTable.putAction(StaticStates.LIVE, QAKcmds.ControlRemoveFromRegistry.id) {
            println("$name:: received remove_from_registry -> de-registering myself from the registry.")

            doControlRemoveFromRegistry()
        }

        transitionTable.putAction(StaticStates.SLEEP, QAKcmds.ControlRemoveFromRegistry.id) {
            println("$name:: received remove_from_registry -> de-registering myself from the registry.")

            doControlRemoveFromRegistry()
        }


        transitionTable.putAction(DynamicStates.DEACTIVATED, QAKcmds.ControlAddToRegistry.id) {
            println("$name:: received add_to_registry -> registering to registry.")

            doUnchained()
        }
    }

    private suspend fun doControlRemoveFromRegistry() {
        state = DynamicStates.DEACTIVATED

        val msg = QAKcmds.RegistryRemoveLink("$name|$ctx|$act")
        emit(msg.id, msg.cmd)

        turnOffLed()
    }
}