package org.joltsphere.misc

/** Creates a fully fleshed out ReLu regression neural network
 *  Feedforward on command, backpropagate to reduce error.
 *
 * @param numberOfInputs a value that sets the number of input neurons
 * @param numberOfOutputs a value that sets the number of output neurons
 * @param hiddenLayerConfiguration an int-array with the size of it setting the number of hidden layers, and with each corresponding value setting the number of neurons in that hidden layer
 */
class NeuralNetwork(val numberOfInputs: Int, val numberOfOutputs: Int, val hiddenLayerConfiguration: IntArray) {

    var cost = 0f
    val numberOfHiddenLayers = hiddenLayerConfiguration.size
    val networkSynapses: Array<Array<FloatArray>>

    init {
        val layerSizes = IntArray(2+numberOfHiddenLayers)
        layerSizes[0] = numberOfInputs // first layer consists of the input neurons
        layerSizes[layerSizes.size-1] = numberOfOutputs  // last layer is just the output neurons
        for (i in 1 until layerSizes.size-1) layerSizes[i] = hiddenLayerConfiguration[i-1]+1 // for each of the hidden layers, set the layer size to the number of hidden layer neurons

        networkSynapses = Array(layerSizes.size-1, // create a weight matrix for every layer of the neural network except the output
                { i -> (randomMatrix(layerSizes[i]+1,layerSizes[i+1]).multiply(2f)).subtract(1f) })
                // the matrix size is equal to (layer size + a bias weight) X (layer size of the next layer); creates a random matrix *2 -1 to put all values from -1 to 1
    }

    /** Feeds an array of inputs into the network to predict a set of outputs to the best of its ability in its current state.
     * @param inputs an array of inputs equivalent to the number of input neurons
     * @return an array of outputs corresponding to the output neurons with respect to the inputs
     */
    fun feedforward(inputs: FloatArray): FloatArray {
        val input = Array(1, { inputs })
        val output = Array(1, { FloatArray(numberOfOutputs) })
        var justALayer = input // this is just a layer variable that is recycled throughout the feedforward process
        for (i in 0 until networkSynapses.size) {
            justALayer = addBiasColumn(justALayer).dot(networkSynapses[i]) // get next layer by multiplying current layer with a bias column by the synapses for that layer
            if (i != networkSynapses.size-1) justALayer = leakyReLu(justALayer) // if it isn't the last layer, which has no activation, then apply the the leaky ReLu activation function
        }
        return output[0] // there's only one array value anyway
    }

    /** Reduces the error of the neural network using gradient descent. Increasing the size of the training data mini-batches will increase the accuracy of the error reduction.
     * - Note that the training inputs matrix and target outputs matrix should have the same number of rows.
     * @param trainingInputs a matrix of inputs with each row being a set of inputs for each input neuron
     * @param targetOutputs a matrix with rows of the target output values corresponding to that row on the the training inputs
     * @param learningRate how large of a gradient descent step the network takes, if set too high, it will converge quickly but will be inaccurate
     *  - you must set this below 0.03, for accuracy maybe even around 0.005
     */
    fun backpropagate(trainingInputs: Array<FloatArray>, targetOutputs: Array<FloatArray>, learningRate: Float) {
        var layers = Array(networkSynapses.size+1, { Array(1, { FloatArray(1) } ) }) // array of every layer after activation
        val lastLayer = layers.size - 1
        layers[0] = trainingInputs
        for (i in 1 until layers.size) {
            layers[i] = addBiasColumn(layers[i-1]).multiply(networkSynapses[i-1]) // get this layer by multiplying previous layer with a bias column by the synapses for that layer
            if (i != layers.size-1) layers[i] = leakyReLu(layers[i]) // if it isn't the last layer, which has no activation, then apply the the leaky ReLu activation function
        }
        val predictedOutputs = layers.last() // the final layer contains the network predictions
        val layerErrors = layers // an array of matrices that contain the error for each layer
        cost = ( (predictedOutputs.subtract(targetOutputs)).power(2f) ).mean() // cost is found by taking the average of all the squared errors
        layerErrors[lastLayer] = 2f.multiply(targetOutputs.subtract(predictedOutputs)) // derivative of cost function
        val layerDeltas = layerErrors // last layer is equivalent already due to the lack of a final layer activation function
        val deltaNetworkSynapses = networkSynapses
        for (i in networkSynapses.size-1 downTo 0) {
            //var layerDelta = layerErrors[lastLayer] // if this is last layer, the delta equals the error of the last layer due to the lack of a final layer activation function
            if (i != networkSynapses.size-1) {
                // the error of the layer this synapse[i] feeds into = to the delta of the layer ahead dot product multiplied by the transpose of the synapses ahead
                layerErrors[i+1] = layerDeltas[i+2].dot(networkSynapses[i+1].T())
                // the delta change of the layer this synpase[i] feeds into = the layer's error element multiplied by the slope of that layer's activation
                layerDeltas[i+1] = layerErrors[i+1].multiply(leakyReLuPrime(layers[i+1]))
            }
            // the change in synapse matrix i equals the transpose of the layer i (that feeds into synapses i) dot-product multiplied by the layer delta of the layer ahead
            deltaNetworkSynapses[i] = learningRate.multiply(layers[i].T().dot(layerDeltas[i+1]))
        }
        for (i in 0 until networkSynapses.size) {
            if (i == networkSynapses.size-1) // if this is the last synapse matrix
                networkSynapses[i] = networkSynapses[i].add(deltaNetworkSynapses[i]) // just add the changes of the matrix
            else // if a normal layer
                networkSynapses[i] = networkSynapses[i].add(removeBiasColumn(deltaNetworkSynapses[i])) // remove the bias column before adding the changes
        }
    }

    /** Adds a columns of 1's to your matrix! Have you ever wanted your layer's input values for the next layer to have a 1 in it to simulate a bias!?
     *  Well now you can thanks to our revolutionary, simple, and instant "Add-A-Bias"© matrix technology!
     */
    fun addBiasColumn(matrix: Array<FloatArray>): Array<FloatArray> {
        val output = Array(rows(matrix), { FloatArray(columns(matrix)+1) }) // creates a matrix of the equivalent size but with an extra columns
        for (i in 0 until rows(matrix))
            for (j in 0 until columns(matrix)) output[i][j] = matrix[i][j] // sets the values of the output to the values of the matrix
        for (i in 0 until rows(output)) output[i][columns(output)-1] = 1f // set the bias values
        return output
    }

    /** Simply removes the last column of your matrix...  :\
     */
    fun removeBiasColumn(matrix: Array<FloatArray>): Array<FloatArray> {
        val output = Array(rows(matrix), { FloatArray(columns(matrix)-1) }) // creates a matrix of the equivalent size but with one less column
        for (i in 0 until rows(output))
            for (j in 0 until columns(output)) output[i][j] = matrix[i][j] // sets the values of the output to the values of the matrix
        return output
    }

    // To prevent the death of neurons, a very small gradient is added in to the max function when x < 0
    private val leakyReLuSlope = 0.05f
    fun leakyReLu(x: Array<FloatArray>): Array<FloatArray> {
        val output = x
        for (i in 0 until rows(x))
            for (j in 0 until columns(x))
                if (x[i][j] < 0) output[i][j] *= leakyReLuSlope
        return output
    }
    fun leakyReLuPrime(x: Array<FloatArray>): Array<FloatArray> {
        val output = x
        for (i in 0 until rows(x))
            for (j in 0 until columns(x))
                if (x[i][j] > 0) output[i][j] = 1f
                else output[i][j] = leakyReLuSlope
        return output
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