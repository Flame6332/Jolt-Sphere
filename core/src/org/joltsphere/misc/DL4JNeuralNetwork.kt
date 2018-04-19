package org.joltsphere.misc

import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.DenseLayer

class DL4JNeuralNetwork(val numberOfInputs: Int, val numberOfOutputs: Int, val hiddenLayerConfiguration: IntArray) {

    init {

        val builder = NeuralNetConfiguration.Builder()
        builder.iterations(1000)
        builder.seed(1234)
        builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        builder.biasInit(0.0)
        builder.miniBatch(false)

        val listBuilder = builder.list()
        val hiddenLayerBuilder = DenseLayer.Builder()
        hiddenLayerBuilder.nIn(numberOfInputs)
        hiddenLayerBuilder.nOut(numberOfOutputs)

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