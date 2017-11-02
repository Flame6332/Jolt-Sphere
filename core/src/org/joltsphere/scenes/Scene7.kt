/* 10/30/2017 - Yousef Abdelgaber
*
*   So I made this stick balancing game around two weeks ago, I had quite a bit of fun playing it
*   but the purpose of it wasn't to entertain, it was to make an environment that I could learn to
*   implement a Q-learning model in.
*
 */

package org.joltsphere.scenes

import org.joltsphere.main.JoltSphereMain

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import org.joltsphere.misc.*

class Scene7(internal val game: JoltSphereMain) : Screen {

    internal var world: World
    internal var debugRender: Box2DDebugRenderer

    internal var ppm = JoltSphereMain.ppm
    internal var base: Body
    internal var stick: Body
    internal var joint: RevoluteJoint

    var isAutonomousEnabled = false
    val learnRate = 0.25f
    val discountFac = 0.99f
    val probabilityOfExploration = 0.09f
    val actionLength = 10
    var framesLeftUntilNextAction = actionLength

    // STATES (2^3 TOTAL) x3 actions = 24 Q values
    var horizontalStickSide = 0 // left is -1, right is 1
    var verticalStickSide = 0 // below is -1, above is 1
    var angularRotation = 0 // counterclockwise is -1, clockwise is 1
    var orangeSlice = 0 // 0 through 15 counter clockwise
    val numberOfPizzaSlices = 64

    // THE TWO POSSIBLE ACTIONS AT EACH TIMESTEP IS MOVE-LEFT AND MOVE-RIGHT
    var currentReward = 0f
    var previousReward = 0f
    var actualAngle = 0f

    var qMatrix: Array<FloatArray>
    var sMatrix: Array<FloatArray>
    var prevS = 0
    var prevA = 0
    var stateNum = 0
    var didSomething = false

    var backgroundOffset = 0f

    init {

        /*sMatrix = createMatrix( // All 8 possible states put in a matrix
                row(1f, 1f, 1f), // 1
                row(1f, 1f, -1f), // 2
                row(1f, -1f, 1f), // 3
                row(1f, -1f, -1f), // 4
                row(-1f, 1f, 1f), // 5
                row(-1f, 1f, -1f), // 6
                row(-1f, -1f, 1f), // 7
                row(-1f, -1f, -1f)) // 8*/
        //sMatrix = Array(numberOfPizzaSlices*2, {FloatArray(2)})

        /*for (i in 0 until rows(sMatrix)) {
            sMatrix[i][0] = i.toFloat()
            if (i < 16) sMatrix[i][1] = 1f
            else sMatrix[i][1] = -1f
        }*/
        sMatrix = createStateMatrix(intArrayOf(numberOfPizzaSlices, 4, 2))
        printMatrix(sMatrix)

        //qMatrix = Array(numberOfPizzaSlices*2, { floatArrayOf(0f, 0f) }) // A Q value for each action in each possible state
        qMatrix = randomMatrix(rows(sMatrix), 2)

        world = World(Vector2(0f, -9.8f), false) // ignore inactive objects false
        debugRender = Box2DDebugRenderer()

        val bdef = BodyDef()
        bdef.type = BodyDef.BodyType.KinematicBody
        bdef.position.set(game.width/2f / ppm, game.height*0.1f / ppm)
        base = world.createBody(bdef)
        val fdef = FixtureDef()
        val polygon = PolygonShape()
        polygon.setAsBox(180f/ppm, 90f/ppm)
        fdef.shape = polygon
        fdef.density = 40f
        base.createFixture(fdef)

        bdef.type = BodyDef.BodyType.DynamicBody
        bdef.position.y = game.height/2f/ppm
        stick = world.createBody(bdef)
        polygon.setAsBox(10f/ppm, 700f/ppm)
        fdef.shape = polygon
        stick.createFixture(fdef)

        val rdef = RevoluteJointDef()
        rdef.bodyA = base
        rdef.bodyB = stick
        rdef.localAnchorA.set(0f, 90f/ppm)
        rdef.localAnchorB.set(0f, -700f/ppm)
        joint = world.createJoint(rdef) as RevoluteJoint
        stick.angularDamping = 0.6f

        stick.applyLinearImpulse(Vector2(-10f, 0f), stick.localCenter, true)

    }
/*
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
*/
    fun createStateMatrix(variables: IntArray): Array<FloatArray> {
        var stateCount = 1
        for (variable in variables) stateCount *= variable
        val output = Array(stateCount, {FloatArray(variables.size)})
        var row = 0
        for (i in 0 until variables[0]) {
            for (j in 0 until variables[1])
                for (k in 0 until variables[2]) {
                    output[row][0] = i.toF()
                    output[row][1] = j.toF()
                    output[row][2] = k.toF()
                    row++
                }
        }
        return output
    }
    fun getCurrentState(): FloatArray = floatArrayOf(orangeSlice.toF(), angularRotation.toF(), prevA.toF())

    internal fun update(dt: Float) {

        framesLeftUntilNextAction--

        if (framesLeftUntilNextAction == 0) {
            if (!isAutonomousEnabled && Gdx.input.isKeyJustPressed(Keys.Q)) isAutonomousEnabled = true
            else if (Gdx.input.isKeyJustPressed(Keys.Q)) isAutonomousEnabled = false

            updateRewardAndState()
            checkStateNum()
            if (didSomething) updateQMatrix()

            if (isAutonomousEnabled) {
                if (Math.random() > probabilityOfExploration && currentReward < 0) moveOptimally(dt)
                else {
                    if (Math.random() < 0.5) commandLeft(dt)
                    else commandRight(dt)
                }
            } else {
                if (Gdx.input.isKeyPressed(Keys.LEFT)) commandLeft(dt)
                else if (Gdx.input.isKeyPressed(Keys.RIGHT)) commandRight(dt)
                else commmandNothing(dt)
            }

            framesLeftUntilNextAction = actionLength
            previousReward = currentReward
        }

        backgroundOffset -= base.linearVelocity.x*2.85f

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
        var loopAngle = actualAngle
        for (i in 1..16)
            if (i*(360/numberOfPizzaSlices) > actualAngle) {
                orangeSlice = i-1
                break
            }
        currentReward = currentReward * 2 - 1
        if (actualAngle < 13 || actualAngle > 347) currentReward += 4
        //if (actualAngle < 180+13 && actualAngle > 347-180) currentReward -= 4
        //if (currentReward < 0) currentReward = -0.2f
    }
    fun checkStateNum() {
        for (i in 0 until sMatrix.size) if (sMatrix[i] == getCurrentState()) stateNum = i
    }
    fun updateQMatrix() {
        val reward = currentReward
        qMatrix[prevS][prevA] +=
                learnRate * (reward + discountFac * qMatrix[stateNum].max()!! - qMatrix[prevS][prevA])
    }

    fun moveOptimally(dt: Float) {
        var optimalAction = 0
        for (i in 0 until columns(qMatrix))
            if (qMatrix[stateNum][i] == qMatrix[stateNum].max())
                optimalAction = i
        when {
            optimalAction == 0 -> commandLeft(dt)
            optimalAction == 1 -> commandRight(dt)
        }
    }
    fun commandLeft(dt: Float) {
        didSomething = true
        prevS = stateNum
        moveLeft(dt)
        prevA = 0
    }
    fun commandRight(dt: Float) {
        didSomething = true
        prevS = stateNum
        prevA = 1
        moveRight(dt)
    }
    fun commmandNothing(dt: Float) {
        didSomething = false
        base.setLinearVelocity(0f, 0f)
        //if (base.linearVelocity.x < -0.1f) moveRight(dt)
        //else if (base.linearVelocity.x > 0.1f) moveLeft(dt)
        //else base.setLinearVelocity(0f, base.linearVelocity.y)
    }
    fun moveLeft(dt: Float) = base.setLinearVelocity(-4.5f, 0f)
    fun moveRight(dt: Float) = base.setLinearVelocity(4.5f, 0f)
    //fun moveLeft(dt: Float) = base.setLinearVelocity(base.linearVelocity.x - 20*dt, 0f)
    //fun moveRight(dt: Float) = base.setLinearVelocity(base.linearVelocity.x + 20*dt, 0f)

    override fun render(dt: Float) {
        update(dt)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        world.step(dt, 6, 2)

        game.shapeRender.begin(ShapeType.Filled)

        game.shapeRender.setColor(0.2f,0.2f,0.2f,1f)

        for (i in 0..300) game.shapeRender.rect(i*game.width + backgroundOffset, 0f, game.width*(1/4f), game.height)
        for (i in 0..300) game.shapeRender.rect(-i*game.width + backgroundOffset - game.width, 0f, game.width*(1/4f), game.height)
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