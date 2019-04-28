package stateMachine

class TransitionTable<STATE, INPUT> {
    private var transitionTable = mutableMapOf<Pair<STATE, INPUT>, (suspend () -> Unit)?>()

    fun putAction(state: STATE, input: INPUT, action: suspend () -> Unit) {
        transitionTable[Pair(state, input)] = action
    }

    fun action(state: STATE, input: INPUT): (suspend () -> Unit)? {
        var action: (suspend () -> Unit)? = null

        transitionTable.forEach {
            if (it.key.first!! == state && it.key.second!! == input) {
                action = it.value
            }
        }

        //action= transitionTable[Pair(state,input)] doesn't work, no idea why

        return action
    }
}