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
import com.badlogic.gdx.Input
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

    // STATES
    var angleSliceRear = 0
    var angleSliceFront = 0
    val numberOfPizzaSlices = 12
    var angularRotRear = 0 // counterclockwise is -1, clockwise is 1
    var angularRotFront = 0 // counterclockwise is -1, clockwise is 1

    var isAutonomousEnabled = false
    val learnRate = 0.25f
    val discountFac = 0.99f
    val probabilityOfExploration = 0.1f
    val actionLength = 1/30f
    var timeLeftUntilNextAction = actionLength
    var currentReward = 0f
    var previousReward = 0f
    var actualAngle = 0f

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

        sMatrix = createPotentialityMatrix(intArrayOf(numberOfPizzaSlices, numberOfPizzaSlices, 2, 2))
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
        polygon.setAsBox(1f, 0.4f)
        fdef.shape = polygon
        fdef.density = 10f
        torso.createFixture(fdef)
        polygon.setAsBox(0.05f, 0.4f)
        fdef.shape = polygon
        legFront.createFixture(fdef)
        legRear.createFixture(fdef)

        bdef.type = BodyDef.BodyType.StaticBody
        bdef.position.y = 0f
        ground = world.createBody(bdef)
        polygon.setAsBox(game.width*50/ppm, 90/ppm)
        fdef.shape = polygon
        ground.createFixture(fdef)

        val rdef = RevoluteJointDef()
        rdef.bodyA = torso
        rdef.bodyB = legRear
        rdef.localAnchorA.set(-1f, -0.4f)
        rdef.localAnchorB.set(0f, 0.4f)
        jointRear = world.createJoint(rdef) as RevoluteJoint
        rdef.bodyB = legFront
        rdef.localAnchorA.set(1f, 0.4f)
        jointFront = world.createJoint(rdef) as RevoluteJoint

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
    fun getCurrentState(): FloatArray = floatArrayOf(angleSliceRear.toF(), angleSliceFront.toF(), angularRotRear.toF(), angularRotFront.toF())

    internal fun update(dt: Float) {

        if (!isAutonomousEnabled && Gdx.input.isKeyJustPressed(Input.Keys.Q)) isAutonomousEnabled = true
        else if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) isAutonomousEnabled = false

        if (!isFastForwarding && Gdx.input.isKeyJustPressed(Input.Keys.F)) isFastForwarding = true
        else if (Gdx.input.isKeyJustPressed(Input.Keys.F)) isFastForwarding = false

        if (Gdx.input.isKeyPressed(Input.Keys.PERIOD)) playbackSpeed++
        else if (Gdx.input.isKeyPressed(Input.Keys.COMMA) && playbackSpeed != 1) playbackSpeed--

        if (!isFastForwarding) stepSimulation(dt)
        else for (i in 1..playbackSpeed) stepSimulation(1/30f)

        if (isExplorationEnabled && Gdx.input.isKeyJustPressed(Input.Keys.E)) isExplorationEnabled = false
        else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) isExplorationEnabled = true

    }

    fun stepSimulation(dt: Float) {
        timeLeftUntilNextAction-=dt

        if (timeLeftUntilNextAction <= 0) {

            updateRewardAndState()
            checkStateNum()
            updateQMatrix()

            if (isAutonomousEnabled) {
                if (Math.random() < probabilityOfExploration && isExplorationEnabled) {
                    val rand = Math.random()
                    didJustExplore = true
                    if (rand < 1/3f) commandLeft(dt)
                    else if (rand > 2/3f) commandRight(dt)
                    else commandNothing(dt)
                }
                else moveOptimally(dt)
            } else {
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) commandLeft(dt)
                else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) commandRight(dt)
                else commandNothing(dt)
            }

            timeLeftUntilNextAction = actionLength
            previousReward = currentReward
        }
        world.step(dt, 6, 2)
        backgroundOffset -= base.linearVelocity.x*2.85f
        secondsPassed += dt
    }

    fun updateRewardAndState() {
        // ANGLE = 0 is the upright postion of the stick and it increases to 360 degrees all the way around counterclockwise
        actualAngle = (joint.jointAngle*180f/Math.PI).toFloat() // convert radians to degrees
        while (actualAngle < 0) actualAngle += 360
        while (actualAngle > 360) actualAngle -= 360

        if (joint.jointSpeed > 1.5f) angularRotation = 3
        else if (joint.jointSpeed < 1.5f && joint.jointSpeed > 0) angularRotation = 2
        else if (joint.jointSpeed < 0f && joint.jointSpeed > -1.5f) angularRotation = 1
        else if (joint.jointSpeed < -1.5f) angularRotation = 0

        if (actualAngle < 180) horizontalStickSide = -1
        else if (actualAngle > 180) horizontalStickSide = 1
        if (actualAngle < 90 && actualAngle > 270) verticalStickSide = 1
        else verticalStickSide = -1
        if (horizontalStickSide == -1) currentReward = (180f - actualAngle) / 180f
        else if (horizontalStickSide == 1) currentReward = (actualAngle - 180f) / 180f
        orangeSlice = 0
        for (i in 1..numberOfPizzaSlices)
            if (i.toF()*(360f/numberOfPizzaSlices) > actualAngle) {
                orangeSlice = i-1
                break
            }
        currentReward = currentReward * 2 - 1
        //currentReward = 0f
        if (actualAngle < 3 || actualAngle > 357) currentReward += 5
        if (actualAngle < 180+13 && actualAngle > 347-180) currentReward -= 4
        //if (currentReward < 0) currentReward = -0.2f
    }
    fun checkStateNum() {
        for (i in 0 until sMatrix.size)
            if (sMatrix[i][0].toInt() == getCurrentState()[0].toInt() &&
                    sMatrix[i][1].toInt() == getCurrentState()[1].toInt()) stateNum = i
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

    fun moveOptimally(dt: Float) {
        didJustExplore = false
        var optimalAction = 0
        for (i in 0 until columns(qMatrix))
            if (qMatrix[stateNum][i] == qMatrix[stateNum].max())
                optimalAction = i
        when {
            optimalAction == 0 -> commandLeft(dt)
            optimalAction == 1 -> commandRight(dt)
            optimalAction == 2 -> commandNothing(dt)
        }
    }
    fun commandLeft(dt: Float) {
        prevS = stateNum
        moveLeft(dt)
        prevA = 0
    }
    fun commandRight(dt: Float) {
        prevS = stateNum
        prevA = 1
        moveRight(dt)
    }
    fun commandNothing(dt: Float) {
        prevS = stateNum
        prevA = 2
        //base.setLinearVelocity(0f, 0f)
        if (base.linearVelocity.x < -0.1f) moveRight(dt)
        else if (base.linearVelocity.x > 0.1f) moveLeft(dt)
        else base.setLinearVelocity(0f, base.linearVelocity.y)
    }
    //val speed = 6f
    val maxSpeed = 30f
    //fun moveLeft(dt: Float) = base.setLinearVelocity(-speed, 0f)
    //fun moveRight(dt: Float) = base.setLinearVelocity(speed, 0f)
    fun moveLeft(dt: Float) { if (base.linearVelocity.x > -maxSpeed) base.setLinearVelocity(base.linearVelocity.x - 25*dt, 0f) }
    fun moveRight(dt: Float) { if (base.linearVelocity.x < maxSpeed) base.setLinearVelocity(base.linearVelocity.x + 25*dt, 0f) }

    override fun render(dt: Float) {
        update(dt)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        game.shapeRender.begin(ShapeRenderer.ShapeType.Filled)

        game.shapeRender.setColor(0.1f,0.1f,0.1f,1f)

        for (i in 0..3000) game.shapeRender.rect(i*game.width + backgroundOffset, 0f, game.width*(1/4f), game.height)
        for (i in 0..3000) game.shapeRender.rect(-i*game.width + backgroundOffset - game.width, 0f, game.width*(1/4f), game.height)
        //game.shapeRender.rect(0.5f*game.width + backgroundOffset, 0f, game.width*(1/4f), game.height)
        //game.shapeRender.rect(game.width + backgroundOffset, 0f, game.width*(1/4f), game.height)
        //game.shapeRender.rect(1.5f*game.width + backgroundOffset, 0f, game.width*(1/4f), game.height)

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
        game.font.draw(game.batch, "" + orangeSlice, game.width * 0.5f, game.height * 0.5f)
        game.font.draw(game.batch, "R = " + Math.round(currentReward*1000f)/1000f, game.width * 0.1f, game.height * 0.1f)
        game.font.draw(game.batch, "R/S = " + Math.round(joint.jointSpeed*1000f)/1000f, game.width * 0.8f, game.height * 0.1f)
        game.font.draw(game.batch, "Current State $stateNum: ${sMatrix[stateNum][0]} ${sMatrix[stateNum][1]}", 20f, game.height - 110f)
        game.font.draw(game.batch, getTimePassed(), 70f, game.height/2f)
        if (isFastForwarding) game.font.draw(game.batch, "Playback Rate: " + 2 * playbackSpeed, 20f, game.height - 50f)

        game.font.color = Color.GREEN
        if (isExplorationEnabled)
            game.font.draw(game.batch, "EXPLORING OPPORTUNITIES", game.width - 1300f, game.height - 180f)
        if (didJustExplore)
            game.font.draw(game.batch, "JUST EXPLORED", game.width - 1200f, game.height - 250f)
        game.font.color = Color.WHITE

        game.batch.end()

        game.phys2DCam.translate(base.position.x-game.width/2f/ppm,0f)
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