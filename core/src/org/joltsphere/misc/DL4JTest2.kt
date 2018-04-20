/* 4/19/2018 - Yousef Abdelgaber
*
*   I'm back. It's been a while. I guess I kind of took a break. It wasn't a good break. I'd say it was more of a dip.
*   But now, I'm back and I'm ready to begin working on what's important. I'm finally 16 now! I've definitely had
*   quite an adventure since I last was at my computer writing code like this. It's been taking me a while but now I
*   think it's all coming back to me. I made some new friends, had quite a wild experience with crypto-currencies,
*   mastered 3D printing, and invested a huge chunk of my life into a robotics competition. Now, though, I'm ready to
*   try implementing deep Q-learning again and this time I'm using the DL4J library so that I can easily build stable
*   networks without worrying about optimization or all the miserable stuff! This is my second test of a DL4J ReLu
*   feed-forward network.
*
*/

package org.joltsphere.misc

import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.distribution.UniformDistribution
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions

object DL4JTest2 {

    @JvmStatic
    fun main(args: Array<String>) {

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

        // dataset object
        val ds: DataSet = DataSet(input, labels)



        val conf: MultiLayerConfiguration = NeuralNetConfiguration.Builder()
                .seed(123)
                .iterations(2000)
                .learningRate(0.01)
                .useDropConnect(false)
                .miniBatch(false)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .biasInit(0.0)
                .regularization(true).l2(0.001)
                .list()
                .layer(0, DenseLayer.Builder().nIn(2).nOut(4).activation(Activation.LEAKYRELU)
                        .weightInit(WeightInit.DISTRIBUTION).dist(UniformDistribution(0.0,1.0))
                        .build())
                .layer(1, OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
                        .activation("relu")
                        .nIn(2).nOut(2).build())
                .backprop(true).pretrain(false)
                .build();


    }

}