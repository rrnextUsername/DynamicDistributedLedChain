package actors

import it.unibo.`is`.interfaces.IObserver
import it.unibo.bls.devices.gui.ButtonAsGui
import it.unibo.kactor.ActorBasic
import it.unibo.kactor.ApplMessage
import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import stateMachine.QAKcmds
import stateMachine.TransitionTable
import java.util.*

class ButtonGuiObserver(private val actor: ActorBasic) : IObserver {

    override fun update(o: Observable?, arg: Any?) {
        GlobalScope.launch {
            val msg = QAKcmds.ButtonClicked("click")
            MsgUtil.sendMsg(msg.id, msg.cmd, actor)
        }
    }

}

class ButtonGuiActor(name: String, scope: CoroutineScope) : ActorBasic(name, scope) {
    enum class States {
        WORKING;
    }

    private val transitionTable = TransitionTable<States, String>()
    private var state: States = States.WORKING

    init {
        val button = ButtonAsGui.createButton("click")
        button.addObserver(ButtonGuiObserver(this))

        transitionTableSetup()
    }

    private fun transitionTableSetup() {
        transitionTable.putAction(States.WORKING, QAKcmds.ButtonClicked.id) {
            println("$name:: received click -> emitting.")
            emitClick()
        }
    }

    override suspend fun actorBody(msg: ApplMessage) {
        print("$name: received $msg | state: $state ")
        transitionTable.action(state, msg.msgId())?.invoke() ?: println(" :: message not recognized -> dropping.")
    }

    private suspend fun emitClick() {
        val msg = QAKcmds.RegistryButtonClicked("click")
        emit(msg.id, msg.cmd)
    }

}

fun main() = runBlocking {
    QakContext.createContexts("localhost", this, "sysDescButton.pl", "sysRules.pl")
}