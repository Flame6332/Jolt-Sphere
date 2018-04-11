package org.joltsphere.misc

import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.api.Layer
import org.deeplearning4j.nn.api.OptimizationAlgorithm
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
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions

object DL4JTest {

    @JvmStatic
    fun main(args: Array<String>) {

        // list of input valeus, 4 training samples with data for 2 input neurons each
        val input: INDArray = Nd4j.zeros(4,2)

        // corresponding list with expected output values, 4 training samples
        // with data  for 2 output neurons each
        val labels: INDArray = Nd4j.zeros(4,2)

        /*
        0,0     1,0
        1,0     0,1
        0,1     0,1
        1,1     1,0
        */

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

        // the network config
        val builder = NeuralNetConfiguration.Builder()
        // how often the training set should be run, needs something above
        // 1000, or a higher learning-rate; found through trial and error
        builder.iterations(2000)
        builder.learningRate(0.01)
        // network randomization seed
        builder.seed(123)
        // not applicable, this network is too small - but for bigger networks it
        // can help that the network will not only recite the training data
        builder.useDropConnect(false)
        // a standard algorithm for moving on the error-plane, this one works best for me,
        // LINE_GRADIENT DESCENT or CONJUGATE_GRADIENT can do the job, too - it's an empirical
        // value which one matches best to your problem
        builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        //initialize bias with empirical value 0
        builder.biasInit(0.0)
        // from "http://deeplearning4j.org/architecture": The networks can
        // process the input more quickly and more accurately by ingesting
        // minibatches 5-10 elements at a time in parallel.
        // this example runs better without, because the dataset is smaller than
        // the mini batch size
        builder.miniBatch(false)

        // create a multilayer network with 2 layers (includiing the output layer,
        // excluding the input layer
        val listBuilder = builder.list()

        val hiddenLayerBuilder = DenseLayer.Builder()
        // 2 input connections - simultaneously defines the number of input neurons, because
        // it's the first non-input layer
        hiddenLayerBuilder.nIn(2)
        // number of outgoing connections, nOut simulteanously defines the number
        // of neurons in this layer
        hiddenLayerBuilder.nOut(4)
        // put the output through the sigmmoid function, to cap the output
        // value between 0 and 1
        hiddenLayerBuilder.activation(Activation.LEAKYRELU)
        // random initialize weights with values between 0 and 1
        hiddenLayerBuilder.weightInit(WeightInit.DISTRIBUTION)
        hiddenLayerBuilder.dist(UniformDistribution(0.0,1.0))

        // build and set as layer 0
        listBuilder.layer(0, hiddenLayerBuilder.build())

        // MCXENT or NEGATIVELOGLIKELIHOOD (both are mathematically equivalent) work ok for this example - this
        // function calculates the error-value (aka 'cost' or 'loss function value'), and quantifies the goodness
        // or badness of a prediction, in a differentiable way
        // For classification (with mutually exclusive classes, like here), use multiclass cross entropy, in conjunction
        // with softmax activation function
        val outputLayerBuilder = OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
        // must be the same amount as neurons in the layer before
        outputLayerBuilder.nIn(4)
        // two neurons in this layer
        outputLayerBuilder.nOut(2)
        outputLayerBuilder.activation(Activation.SOFTMAX)
        outputLayerBuilder.weightInit(WeightInit.DISTRIBUTION)
        outputLayerBuilder.dist(UniformDistribution(0.0,1.0))
        listBuilder.layer(1, outputLayerBuilder.build())

        // no pretrain phase for this net
        listBuilder.pretrain(false)

        // seems to be mandatory
        // according to agibsonccc: You typically only use that with
        // pretrain(true) when you want to do pretrain/finetune without changing
        // the previous layers finetuned weights that's for autoencoders and
        // rbms
        listBuilder.backprop(true)

        // build and initthe network, will check if everything is configured correct
        val conf: MultiLayerConfiguration = listBuilder.build()
        val net = MultiLayerNetwork(conf)
        net.init()

        // add a listener which outputs the error every 100 parameter updates
        net.setListeners(ScoreIterationListener(300))

        // C&P from GravesLSTMCharModellingExample
        // Print the number of parameters in the network (and for each layer)
        val layers: Array<out Layer> = net.layers!!
        var totalNumParams = 0
        for (i in 0 until layers.size) {
            val nParams = layers[i].numParams()
            println("Number of parameters $i: $nParams")
            totalNumParams += nParams
        }
        println("Total number of network parameters: $totalNumParams")

        // here's where the actual learning takes place
        net.fit(ds)

        // create output for everything training sample
        val output: INDArray = net.output(ds.featureMatrix)
        println(output)

        // let Evaluation prints stats how often the right output had the highest value
        val eval = Evaluation(2)
        eval.eval(ds.labels, output)
        println(eval.stats())

    }
}