/* 11/5/2017 - Yousef Abdelgaber
*
*   This will be my first attempt at creating simulated biological creatures that learn to traverse
*   dynamic terrain. This project was inspired by a paper titled "Deep Reinforcement Learning for
*   Terrain-Adaptive Locomotion". In the paper, researchers built two-dimensional simulated creatures
*   that they trained how to run over dynamic terrain using deep Q-learning. I found the results quite
*   interesting and wanted to learn more, so I printed off their research paper and after some reading,
*   I realized that I could do way better. One thing that bothered me is that the 21-link dog only
*   had two legs, I want to see how much better it will be able to run on all fours. Another thing
*   that annoyed me was how they designed the creatures' vision which consisted of a series of values
*   that indicated changes in elevation of the terrain points ahead. I hypothesize that you could get
*   much more realistic and adaptable results if instead you use an array of raycasts that point out in
*   a direction relative to the angle and position of the eye, with many more raycasts in the center of
*   the field of view and than the edges of the 180 degrees. Anyway though, all of these specific details
*   are for the future, right now I just want to get a basic walking creature, these extra features will
*   require DEEP Q-learning anyway and I don't know how to do that yet. I'll figure out deep Q-learning here,
*   then I'll move onto the big picture!
*
*/

package org.joltsphere.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import org.joltsphere.main.JoltSphereMain
import org.joltsphere.misc.*

class Scene8(internal val game: JoltSphereMain) : Screen {

    internal var world: World
    internal var debugRender: Box2DDebugRenderer

    internal var ppm = JoltSphereMain.ppm
    internal var ground: Body
    internal var torso: Body
    internal var legRear: Body
    internal var legFront: Body
    internal var jointRear: RevoluteJoint
    internal var jointFront: RevoluteJoint
    val torsoHW = 1.3f
    val torsoHH = 0.4f
    val legHH = 0.6f

    // STATES (only 5184 * 9 possible actions, only 46000+ Q-values)
    var angleSliceRear = 0
    var angleSliceFront = 0
    var torsoAngleSlice = 0
    var torsoHeightSlice = 0
        val numberOfPizzaSlices = 12
        val numberOfHeightSlices = 3
        val maxHeightSlice = 3*torsoHH*2
    //var angularRotRear = 0 // counterclockwise is -1, clockwise is 1
    //var angularRotFront = 0 // counterclockwise is -1, clockwise is 1

    var isAutonomousEnabled = false
    val learnRate = 0.25f
    val discountFac = 0.99f
    val probabilityOfExploration = 0.1f
    val actionLength = 1/30f
    var timeLeftUntilNextAction = actionLength
    var currentReward = 0f
    var previousReward = 0f

    var isExplorationEnabled = true
    var didJustExplore = false
    var isFastForwarding = false
    var playbackSpeed = 1
    var secondsPassed = 0f

    var qMatrix: Array<FloatArray>
    var sMatrix: Array<FloatArray>
    var aMatrix: Array<FloatArray>
    var prevS = 0
    var prevA = 0
    var stateNum = 0

    init {

        sMatrix = createPotentialityMatrix(intArrayOf(numberOfPizzaSlices, numberOfPizzaSlices, numberOfPizzaSlices, numberOfHeightSlices))
        aMatrix = createPotentialityMatrix(intArrayOf(3,3)) // rotate rear-joint: left,right,stop; rotate front-joint: left,right,stop

        qMatrix = randomMatrix(rows(sMatrix), rows(aMatrix))

        world = World(Vector2(0f, -9.8f), false) // ignore inactive objects false
        debugRender = Box2DDebugRenderer()

        val bdef = BodyDef()
        bdef.type = BodyDef.BodyType.DynamicBody
        bdef.position.set(game.width/2f /ppm, game.height*0.1f /ppm)
        torso = world.createBody(bdef)
        legFront = world.createBody(bdef)
        legRear = world.createBody(bdef)
        val fdef = FixtureDef()
        val polygon = PolygonShape()
        polygon.setAsBox(torsoHW, torsoHH)
        fdef.shape = polygon
        fdef.density = 10f
        torso.createFixture(fdef)
        polygon.setAsBox(0.05f, legHH)
        fdef.shape = polygon
        legFront.createFixture(fdef)
        legRear.createFixture(fdef)

        bdef.type = BodyDef.BodyType.StaticBody
        bdef.position.y = 0f
        ground = world.createBody(bdef)
        polygon.setAsBox(game.width*5000/ppm, 90/ppm)
        fdef.shape = polygon
        ground.createFixture(fdef)

        val rdef = RevoluteJointDef()
        rdef.bodyA = torso
        rdef.bodyB = legRear
        rdef.localAnchorA.set(-torsoHW, -torsoHH+0.03f)
        rdef.localAnchorB.set(0f, legHH)
        jointRear = world.createJoint(rdef) as RevoluteJoint
        rdef.bodyB = legFront
        rdef.localAnchorA.set(torsoHW, -torsoHH+0.03f)
        jointFront = world.createJoint(rdef) as RevoluteJoint
        jointRear.enableMotor(true)
        jointFront.enableMotor(true)
        jointRear.maxMotorTorque = 500f
        jointFront.maxMotorTorque = 500f
        //jointRear.enableLimit(true)
        //jointFront.enableLimit(true)
        jointRear.setLimits(-95*Math.PI.toF()/180f, 95f*Math.PI.toF()/180f)
    }
/* EXAMPLE POTENTIALITY MATRICES (4 * 2 * 2 = 16)
0 0 0
0 0 1
0 1 0
0 1 1
1 0 0
1 0 1
1 1 0
1 1 1
2 0 0
2 0 1
2 1 0
2 1 1
3 0 0
3 0 1
3 1 0
3 1 1
*//*
0 0 0
0 0 1
0 0 2
0 0 3
0 1 0
0 1 1
0 1 2
0 1 3
1 0 0
1 0 1
1 0 2
1 0 3
1 1 0
1 1 1
1 1 2
1 1 3
*//*
0 0 0
0 0 1
0 1 0
0 1 1
0 2 0
0 2 1
0 3 0
0 3 1
1 0 0
1 0 1
1 1 0
1 1 1
1 2 0
1 2 1
1 3 0
1 3 1
*/
    fun createPotentialityMatrix(variables: IntArray): Array<FloatArray> {
        var stateCount = 1
        for (variable in variables) stateCount *= variable
        val output = Array(stateCount, {FloatArray(variables.size)})
        var valueRepetitions = stateCount // the number of times to repeat a number on that column
        for (i in 0 until variables.size) { // for every column
            var row = 0
            valueRepetitions /= variables[i] // decrease the repetition count by one division
            for (r in 1..stateCount/(variables[i]*valueRepetitions)) { // repeats the sequence over and over to fill the whole matrix
                for (j in 0 until variables[i]) { // for all the possible values for that column
                    for (k in 1..valueRepetitions) { // for each repetition
                        output[row][i] = j.toF()
                        row++
                    }
                }
            }
        }
        return output
    }
    fun getCurrentState(): FloatArray =
            floatArrayOf(angleSliceRear.toF(), angleSliceFront.toF(), torsoAngleSlice.toF(), torsoHeightSlice.toF())

    internal fun update(dt: Float) {

        if (!isAutonomousEnabled && Gdx.input.isKeyJustPressed(Keys.Q)) isAutonomousEnabled = true
        else if (Gdx.input.isKeyJustPressed(Keys.Q)) isAutonomousEnabled = false

        if (!isFastForwarding && Gdx.input.isKeyJustPressed(Keys.F)) isFastForwarding = true
        else if (Gdx.input.isKeyJustPressed(Keys.F)) isFastForwarding = false

        if (Gdx.input.isKeyPressed(Keys.PERIOD)) playbackSpeed++
        else if (Gdx.input.isKeyPressed(Keys.COMMA) && playbackSpeed != 1) playbackSpeed--

        if (!isFastForwarding) stepSimulation(dt)
        else for (i in 1..playbackSpeed) stepSimulation(1/30f)

        if (isExplorationEnabled && Gdx.input.isKeyJustPressed(Keys.E)) isExplorationEnabled = false
        else if (Gdx.input.isKeyJustPressed(Keys.E)) isExplorationEnabled = true

    }

    fun stepSimulation(dt: Float) {
        timeLeftUntilNextAction-=dt

        if (timeLeftUntilNextAction <= 0) {

            updateRewardAndState()
            checkStateNum()
            updateQMatrix()

            if (isAutonomousEnabled) {
                if (Math.random() < probabilityOfExploration && isExplorationEnabled) {
                    didJustExplore = true
                    command(Misc.randomInt(0, columns(aMatrix)-1))
                }
                else moveOptimally()
            } else {
                if (Gdx.input.isKeyPressed(Keys.A)) jointRear.motorSpeed = 10f
                else if (Gdx.input.isKeyPressed(Keys.S)) jointRear.motorSpeed = -10f
                else jointRear.motorSpeed = 0f
                if (Gdx.input.isKeyPressed(Keys.K)) jointFront.motorSpeed = 10f
                else if (Gdx.input.isKeyPressed(Keys.L)) jointFront.motorSpeed = -10f
                else jointFront.motorSpeed = 0f
            }

            timeLeftUntilNextAction = actionLength
            previousReward = currentReward
        }
        world.step(dt, 6, 2)
        secondsPassed += dt
    }

    fun command(a: Int) {
        prevS = stateNum
        prevA = a
        val speed = 20f
        when (aMatrix[a][0].toInt()) {
            0 -> jointRear.motorSpeed = -speed
            1 -> jointRear.motorSpeed = speed
            2 -> jointRear.motorSpeed = 0f
        }
        when (aMatrix[a][1].toInt()) {
            0 -> jointFront.motorSpeed = -speed
            1 -> jointFront.motorSpeed = speed
            2 -> jointFront.motorSpeed = 0f
        }
    }

    fun updateRewardAndState() {
        fun reduceInterval(ang: Float): Float {
            var theta = ang
            while (theta < 0) theta += 360
            while (theta > 360) theta -= 360
            return theta
        }
        fun findAngleSlice(degrees: Float, numberOfSlices: Int): Int {
            var slice = 0
            for (i in 1..numberOfSlices)
                if (i.toF()*(360f/numberOfSlices) > degrees) {
                    slice = i-1
                    break
                }
            return slice
        }
        fun findDirectionalSlice(x: Float, min: Float, max: Float, numberOfSlices: Int): Int {
            // 3 slices for height means that anything below 1/5 of the max will be 0, anything above 4/5 of the max is 4, and in between is 1, 2, or 3
            if (x < (1f/numberOfSlices.toF())*(max-min)+min) return 0
            else if (x > ((numberOfSlices.toF()-1)/numberOfSlices.toF())*(max-min)+min) return numberOfSlices-1
            var slice = 1
            for (i in 2 until numberOfSlices) // 2,3,4 // max = 20, min = 10, sliceNum = 5, each slice = 2, anything above 18 = 4, below 12 = 0, 12-14 = 1, 14-16 = 2, 16-18 = 3
                if (x < i.toF()*((max-min)/numberOfSlices)+min) {
                    slice = i-1
                    break
                }
            return slice
        }
        // ANGLE = 0 is the upright postion of the stick and it increases to 360 degrees all the way around counterclockwise
        var actualRearAngle = reduceInterval((jointRear.jointAngle*180f/Math.PI).toFloat()) // convert radians to degrees
        var actualFrontAngle = reduceInterval((jointFront.jointAngle*180f/Math.PI).toFloat()) // convert radians to degrees
        var actualTorsoAngle = reduceInterval((torso.angle*180f/Math.PI).toFloat()) // convert radians to degrees
        angleSliceRear = findAngleSlice(actualRearAngle, numberOfPizzaSlices)
        angleSliceFront = findAngleSlice(actualFrontAngle, numberOfPizzaSlices)
        torsoAngleSlice = findAngleSlice(actualTorsoAngle, numberOfPizzaSlices)
        torsoHeightSlice = findDirectionalSlice(torso.position.y, torsoHH*2, maxHeightSlice, numberOfHeightSlices)

        currentReward = torso.linearVelocity.x*5
        //currentReward += (torso.position.y-torsoHH*2 - 5f
        if (Math.round(actualTorsoAngle) == 180 && torso.position.y < torsoHH*4) {
            //currentReward += 10f
            torso.applyLinearImpulse(0f, 70f, torso.position.x-torsoHW*3, torso.position.y, true)
        }
    }
    fun checkStateNum() {
        for (i in 0 until rows(sMatrix)) {
            var didPassAllChecks = true
            for (j in 0 until columns(sMatrix)) {
                if (sMatrix[i][j].toInt() != getCurrentState()[j].toInt()) didPassAllChecks = false
            }
            if (didPassAllChecks) stateNum = i
        }
    }
    fun getTimePassed(): String {
        val minutes = (secondsPassed/60f).toInt().toString()
        var seconds = (((secondsPassed/60f) - (secondsPassed/60f).toInt())*60).toInt().toString()
        if (seconds.length == 1) seconds = "0" + seconds
        return minutes + ":" + seconds
    }
    fun updateQMatrix() {
        val reward = currentReward
        qMatrix[prevS][prevA] +=
                learnRate * (reward + discountFac * qMatrix[stateNum].max()!! - qMatrix[prevS][prevA])
    }

    fun moveOptimally() {
        didJustExplore = false
        var optimalAction = 0
        for (i in 0 until columns(qMatrix))
            if (qMatrix[stateNum][i] == qMatrix[stateNum].max())
                optimalAction = i
        command(optimalAction)
    }

    override fun render(dt: Float) {
        update(dt)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        game.shapeRender.begin(ShapeRenderer.ShapeType.Filled)

        game.shapeRender.setColor(0.1f,0.1f,0.1f,1f)
        for (i in 0..3000) game.shapeRender.rect(i*game.width, 0f, game.width*(1/4f), game.height)
        for (i in 0..3000) game.shapeRender.rect(-i*game.width, 0f, game.width*(1/4f), game.height)

        game.shapeRender.color = Color.FIREBRICK
        val hyp = 900f
        val posX = game.width/2f
        val posY = 270f
        for (i in 0 until numberOfPizzaSlices) {
            game.shapeRender.setColor(game.shapeRender.color.r - 20f/255f, game.shapeRender.color.g, game.shapeRender.color.b + 10f/255f, 0.5f)
            val y = hyp * Math.sin(i*360/numberOfPizzaSlices*Math.PI/180f+0.5f*Math.PI).toFloat()
            val x = y / Math.tan(i*360/numberOfPizzaSlices*Math.PI/180f+0.5f*Math.PI).toFloat()
            game.shapeRender.rectLine(posX + x, posY + y, posX - x, posY - y, 5f)
        }

        game.shapeRender.end()

        debugRender.render(world, game.phys2DCam.combined)

        game.batch.begin()
        game.font.draw(game.batch, "" + Gdx.graphics.framesPerSecond, game.width * 0.27f, game.height * 0.85f)
        game.font.draw(game.batch, "Current State $stateNum: ${sMatrix[stateNum][0].toInt()} ${sMatrix[stateNum][1].toInt()} ${sMatrix[stateNum][2].toInt()} ${sMatrix[stateNum][3].toInt()}", 20f, game.height - 110f)
        game.font.draw(game.batch, getTimePassed(), 70f, game.height/2f)
        if (isFastForwarding) game.font.draw(game.batch, "Playback Rate: " + 2 * playbackSpeed, 20f, game.height - 50f)
        game.font.draw(game.batch, "ANGLE = " + Math.round(torso.angle*180f/Math.PI*1000f)/1000f, game.width * 0.8f, game.height * 0.25f)
        game.font.draw(game.batch, "HEIGHT = " + Math.round(torso.position.y*1000f)/1000f, game.width * 0.8f, game.height * 0.1f)
        game.font.draw(game.batch, "R = " + Math.round(currentReward*1000f)/1000f, game.width * 0.1f, game.height * 0.1f)

        game.font.color = Color.GREEN
        if (isExplorationEnabled)
            game.font.draw(game.batch, "EXPLORING OPPORTUNITIES", game.width - 1300f, game.height - 180f)
        if (didJustExplore)
            game.font.draw(game.batch, "JUST EXPLORED", game.width - 1200f, game.height - 250f)
        game.font.color = Color.WHITE

        game.batch.end()

        game.phys2DCam.translate(torso.position.x-game.width/2f/ppm,0f)
        game.cam.translate(torso.position.x*ppm - game.width/2f, 0f)
        game.cam.update()
        game.phys2DCam.update()

    }

    override fun dispose() {

    }

    override fun resize(width: Int, height: Int) {
        game.resize(width, height)
    }

    override fun show() {}

    override fun pause() {}

    override fun resume() {}

    override fun hide() {}

}