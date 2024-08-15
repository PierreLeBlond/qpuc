package com.example.qpuc

import com.tinder.StateMachine

sealed class State {
    data object Wait : State()
    data object Top : State()
    data object Buzz : State()
    data object OtherBuzz : State()
    data object Out : State()
}

sealed class Event {
    data object OnTop: Event()
    data object OnBuzz : Event()
    data object OnOtherBuzz : Event()
}

sealed class SideEffect {
}

class HostStateMachine {

    fun createStateMachine(): StateMachine<State, Event, SideEffect> {
        val stateMachine = StateMachine.create<State, Event, SideEffect> {
            initialState(State.Wait)
            state<State.Wait> {
                on<Event.OnTop> {
                    transitionTo(State.Top)
                }
            }
            state<State.Top> {
                on<Event.OnBuzz> {
                    transitionTo(State.Buzz)
                }
                on<Event.OnOtherBuzz> {
                    transitionTo(State.OtherBuzz)
                }
            }
            state<State.OtherBuzz> {
                on<Event.OnTop> {
                    transitionTo(State.Top)
                }
            }
            state<State.Buzz> {
                on<Event.OnTop> {
                    transitionTo(State.Out)
                }
            }
            state<State.Out> {
                on<Event.OnOtherBuzz> {
                    transitionTo(State.OtherBuzz)
                }
            }
        }

        return stateMachine;

    }
}