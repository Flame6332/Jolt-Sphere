package org.joltsphere.misc

import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.distribution.UniformDistribution
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.lossfunctions.LossFunctions

class DL4JNeuralNetwork(val numberOfInputs: Int, val numberOfOutputs: Int, val hiddenLayerConfiguration: IntArray) {

    val net: MultiLayerNetwork

    init {

        val builder = NeuralNetConfiguration.Builder()
        builder.iterations(1000)
        builder.seed(1234)
        builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        builder.biasInit(0.0)
        builder.miniBatch(false)

        // neural network layer comp
        val listBuilder = builder.list()

        val hiddenLayerBuilder = DenseLayer.Builder()
        hiddenLayerBuilder.activation(Activation.LEAKYRELU)
        // random initialize weights with values between 0 and 1
        hiddenLayerBuilder.weightInit(WeightInit.DISTRIBUTION)
        hiddenLayerBuilder.dist(UniformDistribution(0.0,1.0))

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
        outputLayerBuilder.activation(Activation.SOFTMAX)
        outputLayerBuilder.weightInit(WeightInit.DISTRIBUTION)
        outputLayerBuilder.dist(UniformDistribution(0.0,1.0))
        listBuilder.layer(hiddenLayerConfiguration.size, outputLayerBuilder.build())

        listBuilder.pretrain(false)
        listBuilder.backprop(true)

        val conf: MultiLayerConfiguration = listBuilder.build()
        net = MultiLayerNetwork(conf)
        net.init()


    }

    fun feedforward(inputs: FloatArray) { //: FloatArray {

    }

    fun backpropagate(trainingInputs: Array<FloatArray>, targetOutputs: Array<FloatArray>, learningRate: Float, weightDecay: Float) {

    }

    fun getSaveState() {

    }

    fun loadSaveState() {

    }

}