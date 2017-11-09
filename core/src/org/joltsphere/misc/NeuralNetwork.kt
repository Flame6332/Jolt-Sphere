package org.joltsphere.misc

class NeuralNetwork {

    fun setTrainingInputs(inputs: Array<FloatArray>) {

    }

    fun feedforward() {

    }

    fun backpropagate() {

    }

}
/*

Given a transition < s, a, r, s’ >, the Q-table update rule in the previous algorithm must be replaced with the following:

1. Do a feedforward pass for the current state s to get predicted Q-values for all actions.

2. Do a feedforward pass for the next state s’ and calculate maximum overall network outputs max a’ Q(s’, a’).

3. Set Q-value target for action to r + γ * max<a’> Q(s’, a’) (use the max calculated in step 2). For all other actions,
set the Q-value target to the same as originally returned from step 1, making the error 0 for those outputs.

4. Update the weights using backpropagation.


initialize replay memory D
initialize action-value function Q with random weights
observe initial state s
repeat
    select an action a
        with probability ε select a random action
        otherwise select a = argmaxa’Q(s,a’)
    carry out action a
    observe reward r and new state s’
    store experience <s, a, r, s’> in replay memory D

    sample random transitions <ss, aa, rr, ss’> from replay memory D
    calculate target for each minibatch transition
        if ss’ is terminal state then tt = rr
        otherwise tt = rr + γmaxa’Q(ss’, aa’)
    train the Q network using (tt - Q(ss, aa))^2 as loss

    s = s'
until terminated

 */