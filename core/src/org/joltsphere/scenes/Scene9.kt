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

class Scene9(internal val game: JoltSphereMain) : Screen {

    internal var world: World
    internal var debugRender: Box2DDebugRenderer

    internal var ppm = JoltSphereMain.ppm
    internal var ground: Body
    internal var torso: Body
    internal var legUpperRear: Body
    internal var legUpperFront: Body
    internal var legLowerRear: Body
    internal var legLowerFront: Body
    internal var jointHipRear: RevoluteJoint
    internal var jointHipFront: RevoluteJoint
    internal var jointKneeRear: RevoluteJoint
    internal var jointKneeFront: RevoluteJoint
    val jointHipMaxTorque = 500f
    val jointKneeMaxTorque = 300f
    val jointSpeed = 10f
    val torsoHW = 1.3f
    val torsoHH = 0.4f
    val legHH = 0.5f

    var isAutonomousEnabled = false
    val learningRate = 0.02f
    val discountFac = 0.95f
    val probabilityOfExploration = 0.05f
    val replayMemoryCapacity = 1800
    val minibatchSize = 400
    val hiddenLayerConfig = intArrayOf(20,10)
    val numberOfActions = 3
    val actionLength = 1/30f
    var timeLeftUntilNextAction = actionLength
    var currentReward = 0f
    val aiHipRear: DeepQLearner
    val aiHipFront: DeepQLearner
    val aiKneeRear: DeepQLearner
    val aiKneeFront: DeepQLearner

    var isExplorationEnabled = true
    var isFastForwarding = false
    var playbackSpeed = 1
    var secondsPassed = 0f

    init {

        world = World(Vector2(0f, -9.8f), false) // ignore inactive objects false
        debugRender = Box2DDebugRenderer()

        val bdef = BodyDef()
        bdef.type = BodyDef.BodyType.DynamicBody
        bdef.position.set(game.width/2f /ppm, game.height*0.1f /ppm)
        torso = world.createBody(bdef)
        legUpperFront = world.createBody(bdef)
        legUpperRear = world.createBody(bdef)
        legLowerFront = world.createBody(bdef)
        legLowerRear = world.createBody(bdef)
        val fdef = FixtureDef()
        val polygon = PolygonShape()
        polygon.setAsBox(torsoHW, torsoHH)
        fdef.shape = polygon
        fdef.density = 10f
        fdef.filter.maskBits = 1
        fdef.filter.categoryBits = 2
        torso.createFixture(fdef)
        polygon.setAsBox(0.05f, legHH)
        fdef.shape = polygon
        legUpperFront.createFixture(fdef)
        legUpperRear.createFixture(fdef)
        legLowerFront.createFixture(fdef)
        legLowerRear.createFixture(fdef)
        /*fun partSetFilterData(body: Body) {
            //body.fixtureList.first().filterData.categoryBits = 3
            //body.fixtureList.first().filterData.maskBits = 2
        //}
        partSetFilterData(torso)
        partSetFilterData(legLowerRear)
        partSetFilterData(legUpperRear)
        partSetFilterData(legLowerFront)
        partSetFilterData(legUpperFront)
        println("TORSO MASK BITS: ${torso.fixtureList.first().filterData.maskBits}")*/
        bdef.type = BodyDef.BodyType.StaticBody
        bdef.position.y = 0f
        ground = world.createBody(bdef)
        polygon.setAsBox(game.width*5000/ppm, 90/ppm)
        fdef.shape = polygon
        fdef.filter.categoryBits = 1
        fdef.filter.maskBits = 2

        ground.createFixture(fdef)

        val rdef = RevoluteJointDef()
        rdef.bodyA = torso
        rdef.bodyB = legUpperRear
        rdef.localAnchorA.set(-torsoHW, -torsoHH+0.03f)
        rdef.localAnchorB.set(0f, legHH)
        jointHipRear = world.createJoint(rdef) as RevoluteJoint

        rdef.bodyB = legUpperFront
        rdef.localAnchorA.set(torsoHW, -torsoHH+0.03f)
        jointHipFront = world.createJoint(rdef) as RevoluteJoint

        rdef.bodyA = legUpperFront
        rdef.bodyB = legLowerFront
        rdef.localAnchorA.set(0f, -legHH) // local anchor B is already set to the top of the leg height
        jointKneeFront = world.createJoint(rdef) as RevoluteJoint
        rdef.bodyA = legUpperRear
        rdef.bodyB = legLowerRear
        jointKneeRear = world.createJoint(rdef) as RevoluteJoint

        jointHipRear.enableMotor(true)
        jointHipFront.enableMotor(true)
        jointHipRear.maxMotorTorque = jointHipMaxTorque
        jointHipFront.maxMotorTorque = jointHipMaxTorque
        jointKneeFront.enableMotor(true)
        jointKneeRear.enableMotor(true)
        jointKneeFront.maxMotorTorque = jointKneeMaxTorque
        jointKneeRear.maxMotorTorque = jointKneeMaxTorque
        //jointRear.enableLimit(true)
        //jointFront.enableLimit(true)
        //jointRear.setLimits(-95*Math.PI.toF()/180f, 95f*Math.PI.toF()/180f)

        aiHipRear = DeepQLearner(getCurrentState().size, numberOfActions, hiddenLayerConfig, replayMemoryCapacity, probabilityOfExploration)
        aiHipFront = DeepQLearner(getCurrentState().size, numberOfActions, hiddenLayerConfig, replayMemoryCapacity, probabilityOfExploration)
        aiKneeRear = DeepQLearner(getCurrentState().size, numberOfActions, hiddenLayerConfig, replayMemoryCapacity, probabilityOfExploration)
        aiKneeFront = DeepQLearner(getCurrentState().size, numberOfActions, hiddenLayerConfig, replayMemoryCapacity, probabilityOfExploration)

    }

    fun getCurrentState(): FloatArray = floatArrayOf(
            torso.position.y,
            torso.linearVelocity.x,
            legUpperRear.position.x - torso.position.x,
            legUpperRear.position.y - torso.position.y,
            legLowerRear.position.x - torso.position.x,
            legLowerRear.position.y - torso.position.y,
            legUpperFront.position.x - torso.position.x,
            legUpperFront.position.y - torso.position.y,
            legLowerFront.position.x - torso.position.x,
            legLowerFront.position.y - torso.position.y,
            legUpperRear.linearVelocity.x - torso.linearVelocity.x,
            legUpperRear.linearVelocity.y - torso.linearVelocity.y,
            legLowerRear.linearVelocity.x - torso.linearVelocity.x,
            legLowerRear.linearVelocity.y - torso.linearVelocity.y,
            legUpperFront.linearVelocity.x - torso.linearVelocity.x,
            legUpperFront.linearVelocity.y - torso.linearVelocity.y,
            legLowerFront.linearVelocity.x - torso.linearVelocity.x,
            legLowerFront.linearVelocity.y - torso.linearVelocity.y)

    internal fun update(dt: Float) {

        if (!isAutonomousEnabled && Gdx.input.isKeyJustPressed(Input.Keys.Q)) isAutonomousEnabled = true
        else if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) isAutonomousEnabled = false

        if (!isFastForwarding && Gdx.input.isKeyJustPressed(Input.Keys.T)) isFastForwarding = true
        else if (Gdx.input.isKeyJustPressed(Input.Keys.T)) isFastForwarding = false

        if (Gdx.input.isKeyPressed(Input.Keys.PERIOD)) playbackSpeed++
        else if (Gdx.input.isKeyPressed(Input.Keys.COMMA) && playbackSpeed != 1) playbackSpeed--

        if (!isFastForwarding) stepSimulation(dt)
        else for (i in 1..playbackSpeed) stepSimulation(1/30f)

        if (isExplorationEnabled && Gdx.input.isKeyJustPressed(Input.Keys.E)) isExplorationEnabled = false
        else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) isExplorationEnabled = true

    }

    fun stepSimulation(dt: Float) {
        timeLeftUntilNextAction -= dt

        if (timeLeftUntilNextAction <= 0) {

            if (isAutonomousEnabled) {
                command(aiHipRear.updateStateAndRewardThenSelectAction(getCurrentState(), currentReward, isExplorationEnabled), jointHipRear)
                command(aiHipFront.updateStateAndRewardThenSelectAction(getCurrentState(), currentReward, isExplorationEnabled), jointHipFront)
                command(aiKneeRear.updateStateAndRewardThenSelectAction(getCurrentState(), currentReward, isExplorationEnabled), jointKneeRear)
                command(aiKneeFront.updateStateAndRewardThenSelectAction(getCurrentState(), currentReward, isExplorationEnabled), jointKneeFront)
                println()
                aiHipRear.trainFromReplayMemory(minibatchSize, learningRate, discountFac)
                aiHipFront.trainFromReplayMemory(minibatchSize, learningRate, discountFac)
                aiKneeRear.trainFromReplayMemory(minibatchSize, learningRate, discountFac)
                aiKneeFront.trainFromReplayMemory(minibatchSize, learningRate, discountFac)
            } else {
                if (Gdx.input.isKeyPressed(Input.Keys.D)) jointHipRear.motorSpeed = jointSpeed
                else if (Gdx.input.isKeyPressed(Input.Keys.F)) jointHipRear.motorSpeed = -jointSpeed
                else jointHipRear.motorSpeed = 0f
                if (Gdx.input.isKeyPressed(Input.Keys.J)) jointHipFront.motorSpeed = jointSpeed
                else if (Gdx.input.isKeyPressed(Input.Keys.K)) jointHipFront.motorSpeed = -jointSpeed
                else jointHipFront.motorSpeed = 0f
                if (Gdx.input.isKeyPressed(Input.Keys.C)) jointKneeRear.motorSpeed = jointSpeed
                else if (Gdx.input.isKeyPressed(Input.Keys.V)) jointKneeRear.motorSpeed = -jointSpeed
                else jointKneeRear.motorSpeed = 0f
                if (Gdx.input.isKeyPressed(Input.Keys.N)) jointKneeFront.motorSpeed = jointSpeed
                else if (Gdx.input.isKeyPressed(Input.Keys.M)) jointKneeFront.motorSpeed = -jointSpeed
                else jointKneeFront.motorSpeed = 0f
            }
            timeLeftUntilNextAction = actionLength
        }
        world.step(dt, 6, 2)
        secondsPassed += dt
    }

    fun command(a: Int, joint: RevoluteJoint) {
        val speed = jointSpeed
        when (a) {
            0 -> joint.motorSpeed = -speed
            1 -> joint.motorSpeed = speed
            2 -> joint.motorSpeed = 0f
        }
    }

    fun reduceIntervalRadians(ang: Float): Float {
        var theta = ang
        while (theta < 0) theta += 2f*Math.PI.toF()
        while (theta > 2*Math.PI) theta -= 2f*Math.PI.toF()
        return theta
    }
    fun updateReward() {
        currentReward = torso.linearVelocity.x - 0.5f // punishment for being idle
    }

    fun getTimePassed(): String {
        val minutes = (secondsPassed/60f).toInt().toString()
        var seconds = (((secondsPassed/60f) - (secondsPassed/60f).toInt())*60).toInt().toString()
        if (seconds.length == 1) seconds = "0" + seconds
        return minutes + ":" + seconds
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

        game.shapeRender.end()

        debugRender.render(world, game.phys2DCam.combined)

        game.batch.begin()
        game.font.draw(game.batch, "" + Gdx.graphics.framesPerSecond, game.width * 0.27f, game.height * 0.85f)
        game.font.draw(game.batch, getTimePassed(), 70f, game.height/2f)
        if (isFastForwarding) game.font.draw(game.batch, "Playback Rate: " + 2 * playbackSpeed, 20f, game.height - 50f)
        game.font.draw(game.batch, "ANGLE = " + Math.round(torso.angle*180f/Math.PI*1000f)/1000f, game.width * 0.8f, game.height * 0.25f)
        game.font.draw(game.batch, "HEIGHT = " + Math.round(torso.position.y*1000f)/1000f, game.width * 0.8f, game.height * 0.1f)
        game.font.draw(game.batch, "R = " + Math.round(currentReward*1000f)/1000f, game.width * 0.1f, game.height * 0.1f)

        game.font.color = Color.GREEN
        if (isExplorationEnabled)
            game.font.draw(game.batch, "EXPLORING OPPORTUNITIES", game.width - 1300f, game.height - 180f)
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