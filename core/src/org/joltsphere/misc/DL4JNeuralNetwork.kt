/* 5/23/18 - Yousef Abdelgaber
*
*   So, somehow, all of a sudden I casually implemented the DL4J library after a bit of testing here and there. I guess it all
*   just suddenly clicked and I perfectly implemented replacements for all of the old functions and simply implemented a much better neural
*   network into the code. And, while it needs some tuning in a grid world for regularization and learning rate, along with a better
*   exploration system, but I am very happy to begin progress again!
*
 */

package org.joltsphere.misc

import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.LearningRatePolicy
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.distribution.UniformDistribution
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions

class DL4JNeuralNetwork(val numberOfInputs: Int, val numberOfOutputs: Int, val hiddenLayerConfiguration: IntArray, val learningRate: Float) {

    val net: MultiLayerNetwork

    init {

        val builder = NeuralNetConfiguration.Builder()
        builder.iterations(1)
        builder.seed(1234)
        builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        builder.biasInit(0.01)
        builder.miniBatch(false)
        builder.learningRate = learningRate.toDouble()
        //builder.learningRate = 0.0002
        builder.regularization(false)//.l2(0.0000001)//.dropOut(0.5)

        // neural network layer comp
        val listBuilder = builder.list()

        val hiddenLayerBuilder = DenseLayer.Builder()
        hiddenLayerBuilder.activation(Activation.LEAKYRELU)
        // random initialize weights with values between 0 and 1
        hiddenLayerBuilder.weightInit(WeightInit.DISTRIBUTION)
        hiddenLayerBuilder.dist(UniformDistribution(-0.1,0.1))

        // first hidden layer
        hiddenLayerBuilder.nIn(numberOfInputs)
        hiddenLayerBuilder.nOut(hiddenLayerConfiguration[0])
        listBuilder.layer(0, hiddenLayerBuilder.build())

        for (i in 1 until hiddenLayerConfiguration.size) {
            hiddenLayerBuilder.nIn(hiddenLayerConfiguration[i-1])
            hiddenLayerBuilder.nOut(hiddenLayerConfiguration[i])
            listBuilder.layer(i, hiddenLayerBuilder.build())
        }

        val outputLayerBuilder = OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
        outputLayerBuilder.nIn(hiddenLayerConfiguration[hiddenLayerConfiguration.size-1])
        outputLayerBuilder.nOut(numberOfOutputs)
        outputLayerBuilder.activation(Activation.IDENTITY)
        outputLayerBuilder.weightInit(WeightInit.DISTRIBUTION)
        outputLayerBuilder.dist(UniformDistribution(-0.1,0.1))
        listBuilder.layer(hiddenLayerConfiguration.size, outputLayerBuilder.build())

        listBuilder.pretrain(false)
        listBuilder.backprop(true)

        val conf: MultiLayerConfiguration = listBuilder.build()
        net = MultiLayerNetwork(conf)
        net.init()
        net.setListeners(ScoreIterationListener(400))

    }

    fun feedforward(inputs: FloatArray): FloatArray {
        return net.output(inputs.toINDArray()).toFloatArray()
    }

    fun backpropagate(trainingInputs: Array<FloatArray>, targetOutputs: Array<FloatArray>) {
        net.fit(trainingInputs.toINDArray(), targetOutputs.toINDArray())
    }

    fun getSaveState() {

    }

    fun loadSaveState() {

    }

}

fun Array<FloatArray>.toINDArray(): INDArray {
    val indArray: INDArray = Nd4j.zeros(rows(this), columns(this))
    for (i in 0 until rows(this))
        for (j in 0 until columns(this))
            indArray.putScalar(intArrayOf(i,j), this[i][j])
    return indArray
}
fun INDArray.toMatrix(): Array<FloatArray> {
    val output = Array(this.rows(), { FloatArray(this.columns()) })
    for (i in 0 until this.rows())
        for (j in 0 until this.columns())
            output[i][j] = this.getFloat(i,j)
    return output
}
fun FloatArray.toINDArray(): INDArray {
    val indArray: INDArray = Nd4j.zeros(1, this.size)
    for (i in 0 until this.size)
        indArray.putScalar(intArrayOf(0, i), this[i])
    return indArray
}
fun INDArray.toFloatArray(): FloatArray {
    if (this.rows() != 1)
        throw IllegalArgumentException("Only one row allowed for converting to a float array you dummy!")
    val floatArray = FloatArray(this.columns())
    for (i in 0 until this.columns()) {
        floatArray[i] = this.getFloat(0, i)
    }
    return floatArray
}

fun main(args: Array<String>) {

    val inputs = createMatrix(
            row(0f, 0f, 0f),
            row(0f, 1f, 0f),
            row(1f, 0f, 1f),
            row(1f, 1f, 1f))

    val targetOutputs = createMatrix(
            row(0f, 1f, 0f),
            row(1f, 0f, 1f),
            row(1f, 1f, 1f),
            row(0f, 0f, 0f))


    // list of input values, 4 training samples with data for 2 input neurons each
    val input: INDArray = Nd4j.zeros(4,2)

    // corresponding list with expected output values, 4 training samples
    // with data  for 2 output neurons each
    val labels: INDArray = Nd4j.zeros(4,2)
    // create the first data-set
    // when first input=0 and second input=0
    input.putScalar(intArrayOf(0, 0), 0)
    input.putScalar(intArrayOf(0, 1), 0)

    input.putScalar(intArrayOf(1, 0), 1)
    input.putScalar(intArrayOf(1, 1), 0)

    input.putScalar(intArrayOf(2, 0), 0)
    input.putScalar(intArrayOf(2, 1), 1)

    input.putScalar(intArrayOf(3, 0), 1)
    input.putScalar(intArrayOf(3, 1), 1)


    labels.putScalar(intArrayOf(0, 0), 1)
    labels.putScalar(intArrayOf(0, 1), 0)

    labels.putScalar(intArrayOf(1, 0), 0)
    labels.putScalar(intArrayOf(1, 1), 1)

    labels.putScalar(intArrayOf(2, 0), 0)
    labels.putScalar(intArrayOf(2, 1), 1)

    labels.putScalar(intArrayOf(3, 0), 1)
    labels.putScalar(intArrayOf(3, 1), 0)

    val neuralNet = DL4JNeuralNetwork(2, 2, intArrayOf(4), 0.001f)

    println(neuralNet.net.output(input))
    for (i in 1..1000) neuralNet.backpropagate(input.toMatrix(), labels.toMatrix())
    println(neuralNet.net.output(input))

    //println("Input [0, 1] = " + neuralNet.feedforward(floatArrayOf(0f,1f))[0])
    //println("Input [0, 0.5] = " + neuralNet.feedforward(floatArrayOf(0f,0.5f))[0])

}