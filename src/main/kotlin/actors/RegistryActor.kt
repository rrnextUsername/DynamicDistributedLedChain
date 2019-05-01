package actors

import QAKChainSysUtils
import it.unibo.kactor.ActorBasic
import it.unibo.kactor.ApplMessage
import it.unibo.kactor.QakContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import stateMachine.QAKcmds
import stateMachine.TransitionTable
import java.net.InetAddress

class RegistryActor(name: String, scope: CoroutineScope) : ActorBasic(name, scope) {

    enum class States {
        INIT,
        CHAIN_IDLE,
        CHAIN_ACTIVE;
    }

    data class LinkData(val name: String, val content: String) {
        override fun equals(other: Any?): Boolean {
            return when (other) {
                is LinkData -> name == other.name && content == other.content
                else -> super.equals(other)
            }
        }

        override fun toString(): String {
            return "link(name: $name, content: $content)"
        }
    }

    private var state = States.INIT
    private var lastMessage: ApplMessage? = null
    private val transitionTable = TransitionTable<States, String>()

    var links = mutableListOf<LinkData>()


    init {
        transitionTableSetup()

        val address= InetAddress.getLocalHost()
        println("Registry on: ${address.hostAddress}")
    }

    private fun transitionTableSetup() {
        transitionTable.putAction(States.INIT, QAKcmds.RegistryAddLink.id) {
            println("$name:: received add_link -> adding the first link of the chain.")
            doAddFirstLink(lastMessage!!)
        }


        transitionTable.putAction(States.CHAIN_IDLE, QAKcmds.RegistryAddLink.id) {
            println("$name:: received add_link -> adding link at the end of the chain.")
            doAddLink(lastMessage!!)
        }
        transitionTable.putAction(States.CHAIN_IDLE, QAKcmds.RegistryRemoveLink.id) {
            println("$name:: received remove_link -> removing link from the chain.")
            doRemoveLink(lastMessage!!)
        }
        transitionTable.putAction(States.CHAIN_IDLE, QAKcmds.RegistryButtonClicked.id) {
            println("$name:: received click -> activating the chain.")
            doChainActive()
        }

        transitionTable.putAction(States.CHAIN_ACTIVE, QAKcmds.RegistryAddLink.id) {
            println("$name:: received add_link -> adding link at the end of the chain.")
/*
            val msg= lastMessage!!

            autoMsg(QAKcmds.RegistryButtonClicked.id,"stopping chain to operate on it")
            autoMsg(msg)
            autoMsg(QAKcmds.RegistryButtonClicked.id,"starting chain again")
*/

            doStopChain()
            doAddLink(lastMessage!!)
            doStartChain()
        }
        transitionTable.putAction(States.CHAIN_ACTIVE, QAKcmds.RegistryRemoveLink.id) {
            println("$name:: received remove_link -> removing link from the chain.")

            doStopChain()
            doRemoveLink(lastMessage!!)
            doStartChain()
        }
        transitionTable.putAction(States.CHAIN_ACTIVE, QAKcmds.RegistryButtonClicked.id) {
            println("$name:: received click -> stopping the chain.")
            doChainIdle()
        }
    }

    override suspend fun actorBody(msg: ApplMessage) {
        lastMessage = msg
        print("$name: received $lastMessage")
        transitionTable.action(state, msg.msgId())?.invoke() ?: println(" :: message not recognized -> dropping.")
    }

    private suspend fun doAddFirstLink(msg: ApplMessage) {
        state = States.CHAIN_IDLE

        links.add(LinkData(msg.msgSender(), msg.msgContent()))
        QAKChainSysUtils.createContext(msg.msgContent())

        forward(QAKcmds.ControlChangeNext.id, links[0].content, msg.msgSender())
        delay(10)
        forward(QAKcmds.ControlToken.id, "token initialization", msg.msgSender())
    }

    private suspend fun doAddLink(msg: ApplMessage) {
        links.add(LinkData(msg.msgSender(), msg.msgContent()))

        QAKChainSysUtils.createContext(msg.msgContent())

        forward(QAKcmds.ControlChangeNext.id, links[0].content, msg.msgSender())
        forward(QAKcmds.ControlChangeNext.id, msg.msgContent(), links[links.size - 2].name)
    }

    private suspend fun doRemoveLink(msg: ApplMessage) {
        val linkAt = links.indexOfFirst { ld -> ld == LinkData(msg.msgSender(), msg.msgContent()) }

        val link = links[linkAt]
        val previous = links[if (linkAt == 0) {
            links.size - 1
        } else linkAt - 1]
        val next = links[if (linkAt == links.size - 1) {
            0
        } else linkAt + 1]


        forward(QAKcmds.ControlChangeNext.id, next.content, previous.name)

        links.remove(link)
    }

    private suspend fun doChainIdle() {
        state = States.CHAIN_IDLE

        doStopChain()
    }

    private suspend fun doChainActive() {
        state = States.CHAIN_ACTIVE

        doStartChain()
    }

    private suspend fun doStopChain() {
        val msg = QAKcmds.ControlStop("stopping chain")
        emit(msg.id, msg.cmd)
    }

    private suspend fun doStartChain() {
        val msg = QAKcmds.ControlStart("starting chain")
        emit(msg.id, msg.cmd)
    }


}

fun main() = runBlocking {
    QakContext.createContexts("localhost", this, "sysDescRegistry.pl", "sysRules.pl")
}