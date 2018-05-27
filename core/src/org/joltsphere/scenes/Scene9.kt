/* 11/22/2017 - Yousef Abdelgaber
*
*   I'm back to trying to balance the stick. This time though, I'm going to do it using a deep Q-learning library that I built.
*   Originally, I was trying to implement my deep Q-learner in a creature walking environment. That wasn't working though so I'm
*   throwing back to the original environment.
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

class Scene9(internal val game: JoltSphereMain) : Screen {

    internal var world: World
    internal var debugRender: Box2DDebugRenderer

    internal var ppm = JoltSphereMain.ppm
    internal var base: Body
    internal var stick: Body
    internal var joint: RevoluteJoint

    var isAutonomousEnabled = false
    val learningRate = 0.000001f
    val weightDecay = 0.00001f
    val discountFac = 0.98f
    var explorationProbability = 0.005f
    val replayMemoryCapacity = 10 * 30
    val minibatchSize = 16
    val explorationLength = 2
    val hiddenLayerConfig = intArrayOf(5,5)
    val numberOfActions = 3
    val actionLength = 1/30f
    var timeLeftUntilNextAction = actionLength
    val aiController: DeepQLearner


    // THE TWO POSSIBLE ACTIONS AT EACH TIMESTEP IS MOVE-LEFT AND MOVE-RIGHT
    var currentReward = 0f
    var previousReward = 0f
    var actualAngle = 0f

    var isExplorationEnabled = true
    var isFastForwarding = false
    var playbackSpeed = 1
    var secondsPassed = 0f

    var backgroundOffset = 0f

    init {


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

        aiController = DeepQLearner(getCurrentState().size, numberOfActions, hiddenLayerConfig, replayMemoryCapacity,
                0f, 2f, 1,3, 0.03f, 4)
        aiController.name = "THE ULTIMATE STICK BALANCER"
        aiController.isDebugEnabled = true
    }

    fun getCurrentState(): FloatArray = floatArrayOf(reduceIntervalRadians(joint.jointAngle)-Math.PI.toF(), joint.jointSpeed, base.linearVelocity.x)
    fun reduceIntervalRadians(ang: Float): Float {
        var theta = ang
        while (theta < 0) theta += 2f * Math.PI.toF()
        while (theta > 2 * Math.PI) theta -= 2f * Math.PI.toF()
        return theta
    }

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

        if (aiController.isDebugEnabled && Gdx.input.isKeyJustPressed(Keys.SLASH)) aiController.isDebugEnabled = false
        else if (Gdx.input.isKeyJustPressed(Keys.SLASH)) aiController.isDebugEnabled = true

    }

    fun stepSimulation(dt: Float) {
        timeLeftUntilNextAction-=dt

        if (timeLeftUntilNextAction <= 0) {

            updateReward()

            if (isAutonomousEnabled) {
                val action = aiController.updateStateAndRewardThenSelectAction(
                        getCurrentState(), currentReward, isExplorationEnabled)
                when (action) {
                    0 -> commandNothing(dt)
                    1 -> commandLeft(dt)
                    2 -> commandRight(dt)
                }
                for (i in 1..1) {
                    aiController.trainFromReplayMemory(minibatchSize, learningRate, weightDecay, discountFac)
                }
            } else {
                if (Gdx.input.isKeyPressed(Keys.LEFT)) commandLeft(dt)
                else if (Gdx.input.isKeyPressed(Keys.RIGHT)) commandRight(dt)
                else commandNothing(dt)
            }

            timeLeftUntilNextAction = actionLength
            previousReward = currentReward
        }
        world.step(dt, 6, 2)
        backgroundOffset -= base.linearVelocity.x*2.85f
        secondsPassed += dt
    }

    fun updateReward() {
        // ANGLE = 0 is the upright postion of the stick and it increases to 360 degrees all the way around counterclockwise
        actualAngle = (joint.jointAngle * 180f / Math.PI).toFloat() // convert radians to degrees
        while (actualAngle < 0) actualAngle += 360
        while (actualAngle > 360) actualAngle -= 360
        var horizontalStickSide = 0
        if (actualAngle < 180) horizontalStickSide = -1
        else if (actualAngle > 180) horizontalStickSide = 1
        if (horizontalStickSide == -1) currentReward = (180f - actualAngle) / 180f
        else if (horizontalStickSide == 1) currentReward = (actualAngle - 180f) / 180f
        currentReward = currentReward * 2 - 1
        if (currentReward > 0) currentReward *= 0.1f
        else currentReward = -0.1f
        //currentReward = 0f
        if (actualAngle < 3 || actualAngle > 357) currentReward += 5
        //if (actualAngle < 180 + 13 && actualAngle > 347 - 180) currentReward -= 4
        //if (currentReward < 0) currentReward = -0.2f
    }

    fun getTimePassed(): String {
        val minutes = (secondsPassed/60f).toInt().toString()
        var seconds = (((secondsPassed/60f) - (secondsPassed/60f).toInt())*60).toInt().toString()
        if (seconds.length == 1) seconds = "0" + seconds
        return minutes + ":" + seconds
    }
    /** Returns the actual playback speed of the simulation */
    private fun getActualPlaybackSpeed(): Float = Math.round(playbackSpeed * 2f * Gdx.graphics.framesPerSecond / 60f * 100f) / 100f
    fun commandLeft(dt: Float) {
        moveLeft(dt)
    }
    fun commandRight(dt: Float) {
        moveRight(dt)
    }
    fun commandNothing(dt: Float) {
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

    /** Rounds a value to float place */
    fun Float.round(pos: Float): Float = Math.round(this*(1/pos))/Math.round(1f/pos).toF()

    override fun render(dt: Float) {
        update(dt)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        game.shapeRender.begin(ShapeType.Filled)

        game.shapeRender.setColor(0.1f,0.1f,0.1f,1f)

        for (i in 0..30000) game.shapeRender.rect(i*game.width + backgroundOffset, 0f, game.width*(1/4f), game.height)
        for (i in 0..30000) game.shapeRender.rect(-i*game.width + backgroundOffset - game.width, 0f, game.width*(1/4f), game.height)

        game.shapeRender.color = Color.FIREBRICK
        /*val hyp = 900f
        val posX = game.width/2f
        val posY = 270f
        for (i in 0 until numberOfPizzaSlices) {
            game.shapeRender.setColor(game.shapeRender.color.r - 20f/255f, game.shapeRender.color.g, game.shapeRender.color.b + 10f/255f, 0.5f)
            val y = hyp * Math.sin(i*360/numberOfPizzaSlices*Math.PI/180f+0.5f*Math.PI).toFloat()
            val x = y / Math.tan(i*360/numberOfPizzaSlices*Math.PI/180f+0.5f*Math.PI).toFloat()
            game.shapeRender.rectLine(posX + x, posY + y, posX - x, posY - y, 5f)
        }*/

        game.shapeRender.end()

        debugRender.render(world, game.phys2DCam.combined)

        game.batch.begin()
        game.font.draw(game.batch, "" + Gdx.graphics.framesPerSecond, game.width * 0.27f, game.height * 0.85f)
        game.font.draw(game.batch, "R = " + Math.round(currentReward*1000f)/1000f, game.width * 0.1f, game.height * 0.1f)
        game.font.draw(game.batch, "R/S = " + Math.round(joint.jointSpeed*1000f)/1000f, game.width * 0.8f, game.height * 0.1f)
        game.font.draw(game.batch,
            "Current State: ${getCurrentState()[0].round(0.1f)} ${getCurrentState()[1].round(0.1f)}", 20f, game.height - 110f)
        game.font.draw(game.batch,
            "Q-Value Predictions: ${aiController.latestQValuePredictions[0].round(0.1f)} ${aiController.latestQValuePredictions[1].round(0.1f)} ${aiController.latestQValuePredictions[2].round(0.1f)}",
                20f, game.height - 300f)
        game.font.draw(game.batch, getTimePassed(), 70f, game.height/2f)
        if (isFastForwarding) game.font.draw(game.batch, "Playback Rate: ${2f*playbackSpeed} (${getActualPlaybackSpeed()})" , 20f, game.height - 50f)

        game.font.color = Color.GREEN
        if (isExplorationEnabled) {
            game.font.draw(game.batch, "EXPLORING OPPORTUNITIES", game.width - 1300f, game.height - 180f)
            if (aiController.isExploring) game.font.draw(game.batch, "RANDOM EXPLORATION!!!", game.width - 1400f, game.height - 280f)
        }
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