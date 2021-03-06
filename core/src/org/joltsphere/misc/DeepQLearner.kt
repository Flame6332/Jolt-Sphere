package org.joltsphere.misc

import org.deeplearning4j.exception.DeepLearningException

/** A fully loaded deep Q-learning model, complete with action selection and training from replay memory!
 * - To implement this machine learning algorithim into an environment, simply execute the "updateStateAndRewardThenSelectAction(...)"
 *  function once per time step. Then, whenever you feel like it, train the neural network using the "trainFromReplayMemory(...)" function to increase '
 *  model's accuracy.
 * @param numberOfStateInputs The number of state inputs for the neural network, feel free to go wild with this one!
 * @param numberOfActions The number of actions that the deep Q-learner can choose from. Try to minimize this one as the model '
 *  will converge much faster if it has less quality values to optimize for. This is also the number of output neurons that the neural '
 *  network will contain.
 * @param hiddenLayerConfiguration An array of integers with the length of this array being the number of hidden layers within the neural network '
 *  and each corresponding value being the number of neurons in that layer. Try to not go to crazy with this as it will make optimization more difficult.
 * @param replayMemoryCapacity The maximum number of transitions that the replay memory will hold onto for sampling minibatches from during '
 *  training. The larger this is, the further back data will be sampled from. Keeping this value smaller will give a stronger focus to most recent '
 *  information.
 * @param explorationProbability A decimal probability of exploration taking place if it is enabled. This value should be kept relatively low, '
 *  below around 0.1
 * //@param explorationLength How many time-steps the explored move will be repeated.
 */
class DeepQLearner(val numberOfStateInputs: Int, val numberOfActions: Int, val hiddenLayerConfiguration: IntArray,
                   val replayMemoryCapacity: Int, val learningRate: Float,
                   var rewardMin: Float, var rewardMax: Float,
                   val expLengthMin: Int, val expLengthMax: Int, val expProbMin: Float, expComboMax: Int) {

    val neuralNetwork = DL4JNeuralNetwork(numberOfStateInputs, numberOfActions, hiddenLayerConfiguration, learningRate)
    val replayMemory = ArrayList<Transition>()
    private var lastState = FloatArray(numberOfStateInputs)
    private var lastAction = 0
    var currentReward = 0f
    var explorationTimer = 9
    var expComboCount = 0
    var isExploring = false

    var timesSuccesfullyTrained = 0
    var name = "Neural network"
    var isDebugEnabled = false
    var latestQValuePredictions = FloatArray(numberOfActions)

    var lastExternalAction: Int = 0
    var isNextActionExternal = false

    init {
        if (expLengthMin < 1) throw IllegalArgumentException("Boy, who you playing? Minimum exploration length must be at least 1, not $expLengthMin!")
    }

    //var rewardMax = 0f // highest observed reward
    //var rewardMin = 0f // lowest observed reward

    /** Updates the state and reward for the Q-learner to save into it's transition matrix, then returns the optimal action '
     * based off highest quality predicted Q-value for the current state, with the chance of exploration, if enabled.
     * - Note that you should only call this once per time-step to prevent possible issues.
     * @param currentState The current state of the agent consisting of a float array of different environment variables.
     * @param currentReward The current reward of agent, set negative for punishment.
     * @param isExplorationEnabled If true, there's a previously set chance of exploration, which is required to find an optimal state.
     * @return An action value for the current state.
     */
    fun updateStateAndRewardThenSelectAction(currentState: FloatArray, currentReward: Float, isTerminalState: Boolean, isExplorationEnabled: Boolean): Int {
        this.currentReward = currentReward
        if (currentReward > rewardMax) rewardMax = currentReward
        else if (currentReward < rewardMin) rewardMin = currentReward
        if (isTerminalState) // if is terminal state
            replayMemory.add(Transition(lastState, lastAction, currentReward, currentState, true)) // save termination for later
        else // save normal state transition for later
            replayMemory.add(Transition(lastState, lastAction, currentReward, currentState, false))
        if (replayMemory.size > replayMemoryCapacity) replayMemory.removeAt(0)
        lastState = currentState // updates the last state for next loop around

        if (isTerminalState) {
            explorationTimer == 0
        }
        else if (isNextActionExternal) {
            lastAction = lastExternalAction
            isNextActionExternal = false // resets external action system
        }
        else if (explorationTimer > 1) {
            explorationTimer--
            // last action stays the same until exploration is over
        }
        else if (isExplorationEnabled && Math.random() < optimalExplorationProbability()) {
            lastAction = Misc.randomInt(0, numberOfActions - 1) // chooses random action
            explorationTimer = Misc.randomInt(expLengthMin, expLengthMax)
            isExploring = true
            /*if (isDebugEnabled) {
                val qValues = neuralNetwork.feedforward(currentState) // returns an array of Q-values in the current state
                print("$name EXPLORATION Q-value predictions: "); printMatrix(Array(1, {qValues}))
                println("Max Reward: $rewardMax     Min Reward: $rewardMin")
            }*/
        }
        else {
            val qValues = neuralNetwork.feedforward(currentState) // returns an array of Q-values in the current state
            lastAction = qValues.indexOf(qValues.max()!!) // returns the action of the highest quality
            latestQValuePredictions = qValues
            isExploring = false
            if (isDebugEnabled) {
                //print("$name Q-value predictions: "); printMatrix(Array(1, {qValues}))
                //println("Max Reward: $rewardMax     Min Reward: $rewardMin")
            }
            if (lastAction == -1) throw IllegalStateException("$name is outputting NaN garbage after $timesSuccesfullyTrained training passes")
        }
        return lastAction
    }

    fun actionSelectedExternally(action: Int) {
        isNextActionExternal = true
        lastExternalAction = action
    }

    fun optimalExplorationProbability(): Float { // TODO actually make this function work with varying probability, including a combo spike of probability
        return expProbMin
    }

    /** Increases the accuracy of the neural network Q-function through the randomized collection of training samples from the '
     * replay memory, using temporal difference on each sampled transition to find target Q-values, then the stochastic gradient descent of '
     * the neural network through backpropagation.
     * @param minibatchSize The amount of training samples that will be used in the neural network's backpropagation. Set this '
     *  value too high and the network will take much longer to learn, set it too low, and it will be inaccurate. Note that the model won't begin training '
     *  until there is enough replay memory to satisfy the minibatch size requirement, and if its larger than the maximum capacity of the repley memory '
     *  itself, then the program will throw an error.
     * @param learningRate The learning rate of the neural network. Set this around maybe 0.03, set it too high, and the model will be inaccurate, '
     *  set it too low, and the model will take to long to converge.
     * @param discountFactor The model's focus on long term reward versus short term reward. Set this between 0 and 1, but it's best around 0.9 and above.
     */
    fun trainFromReplayMemory(minibatchSize: Int, discountFactor: Float) {
        if (minibatchSize > replayMemoryCapacity) // make sure training will even take place
            throw IllegalArgumentException("silly boy, the minibatch size $minibatchSize > $replayMemoryCapacity replay memory capacity")
        if (minibatchSize <= replayMemory.size) { // if enough training data has been acquired to satisfy a minibatch training sequence

            val bagOfReplayMemory: ArrayList<Transition> = ArrayList(replayMemory) // creates a copy of the replay memory so that parts can be removed one by one
            val trainingInputMatrix = Array(minibatchSize, {FloatArray(numberOfStateInputs)}) // create input matrix for neural network
            val targetOutputMatrix = Array(minibatchSize, {FloatArray(numberOfActions)})

            for (i in 0 until trainingInputMatrix.size) { // fills all the values of the input and output matrix with a state and target value
                var randomMemoryIndex = Misc.randomInt(0, bagOfReplayMemory.size-1) // chooses a random index from the remaining options of the memory
                    //while (bagOfReplayMemory[randomMemoryIndex].action == -1) { // discards of failures
                    //    randomMemoryIndex = Misc.randomInt(0, bagOfReplayMemory.size-1)
                    //}

                if (i < 0.25f * minibatchSize) { // a slice of training data isn't random, instead focuses on large rewards and punishments
                    var mostSignificantIndex = 0
                    for (j in 0 until bagOfReplayMemory.size)
                        if (bagOfReplayMemory[j].reward.abs() > bagOfReplayMemory[mostSignificantIndex].reward.abs()) // if new index has greater reward
                            mostSignificantIndex = j // update the new index
                    randomMemoryIndex = mostSignificantIndex // no loner random
                }

                val transition = bagOfReplayMemory[randomMemoryIndex]

                trainingInputMatrix[i] = transition.state.copyOf() // sets a row of inputs of the training matrix to a value out of the replay memory
                val feedforwardOutput = neuralNetwork.feedforward(trainingInputMatrix[i])
                targetOutputMatrix[i] = feedforwardOutput // set the target outputs to the predicted outeput so all errors are equal to zero
                //println("Transition: ${transition.state} ${transition.action} ${transition.reward} ${transition.resultingState}")
                if (transition.isTerminal)
                    targetOutputMatrix[i][transition.action] = transition.reward
                else
                    targetOutputMatrix[i][transition.action] = // except the action that we're optimizing for
                        transition.reward + discountFactor * neuralNetwork.feedforward(transition.resultingState).max()!! // Bellman Equation: Q(s, a[i]) = r + γ * max<a’> Q(s’, a’)
                //if (isDebugEnabled) println("Target Output: ${targetOutputMatrix[i][transition.action]}")
                bagOfReplayMemory.removeAt(randomMemoryIndex) // takes the chosen training sample out of the bag
            }
            //println("Input: ")
            //printMatrix(Array(1, {trainingInputMatrix[0]}))
            //println("Output: ")
            //printMatrix(Array(1, {targetOutputMatrix[0]}))
            neuralNetwork.backpropagate(trainingInputMatrix, targetOutputMatrix) // gradient descent to minimize cost through backpropagation
                    //println(targetOutputMatrix.toINDArray())
            timesSuccesfullyTrained++
        }
    }

    /** Stores data for < state, action, reward, and resulting state > transitions; used for replay memory. */
    class Transition(val state: FloatArray, val action: Int, val reward: Float, val resultingState: FloatArray, val isTerminal: Boolean)

}

/*

Given a transition < s, a, r, s’ >, the Q-table update rule in the previous algorithm must be replaced with the following:

1. Do a feedforward pass for the current state s to get predicted Q-values for all actions.

2. Do a feedforward pass for the next state s’ and calculate maximum overall network output max a’ Q(s’, a’).

3. Set Q-value target for action to r + γ * max<a’> Q(s’, a’) (use the max calculated in step 2). For all other actions,
set the Q-value target to the same as originally returned from step 1, making the error 0 for those outputs.

4. Update the weights using backpropagation.


initialize replay memory D
initialize action-value function Q with random weights
observe initial state s
repeat
    select an action a
        with probability ε select a random action
        otherwise select a = argmax<a’>Q(s,a’)
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