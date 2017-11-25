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

class Scene10(internal val game: JoltSphereMain) : Screen {

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
    val torsoHW = 1.6f
    val torsoHH = 0.3f
    val legHH = 0.55f

    var isAutonomousEnabled = false
    val learningRate = 0.00007f
    val weightDecay = 0.0006f
    val discountFac = 0.95f
    var explorationProbability = 0.12f
    val replayMemoryCapacity = 3 * 60 * 30
    val minibatchSize = 20
    val explorationLength = 10
    val hiddenLayerConfig = intArrayOf(30,20,10)
    val numberOfActions = 3
    val actionLength = 1/30f
    var timeLeftUntilNextAction = actionLength
    var currentReward = 0f
    val aiHipRear: DeepQLearner
    val aiHipFront: DeepQLearner
    val aiKneeRear: DeepQLearner
    val aiKneeFront: DeepQLearner

    var isExplorationEnabled = true
    var isOverwritingPreviousSave = false
    var isDebugging = false
    var isFastForwarding = false
    var playbackSpeed = 1
    var secondsPassed = 0f
    var cost = 0f

    init {
        
        world = World(Vector2(0f, -9.8f), false) // ignore inactive objects false
        world.setContactListener(BodyContactListener())
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
        polygon.setAsBox(torsoHW*0.75f, torsoHH, Vector2(-0.25f*torsoHW, 0f), 0f)
        fdef.shape = polygon
        fdef.density = 10f
        fdef.restitution = 0.4f
        fdef.friction = 0.8f
        fdef.filter.maskBits = 1
        fdef.filter.categoryBits = 2
        torso.createFixture(fdef)
        val circle = CircleShape()
        circle.radius = torsoHH*2
        circle.setPosition(Vector2(torsoHW-torsoHH*2, torsoHH))
        fdef.shape= circle
        torso.createFixture(fdef)
        fdef.restitution = 0f
        polygon.setAsBox(0.07f, legHH)
        fdef.shape = polygon
        legUpperFront.createFixture(fdef)
        legLowerFront.createFixture(fdef)
        legLowerRear.createFixture(fdef)
        polygon.setAsBox(0.14f, legHH+torsoHH*0.5f)
        fdef.shape = polygon
        legUpperRear.createFixture(fdef)
        circle.radius = 0.16f
        circle.position = Vector2(0f, -legHH)
        fdef.shape = circle
        legLowerRear.createFixture(fdef).userData = "footRear"
        legLowerFront.createFixture(fdef).userData = "footFront"
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

        ground.createFixture(fdef).userData = "ground"

        val rdef = RevoluteJointDef()
        rdef.bodyA = torso
        rdef.bodyB = legUpperRear
        rdef.localAnchorA.set(-torsoHW, torsoHH-0.05f)
        rdef.localAnchorB.set(0f, legHH+torsoHH*0.5f)
        jointHipRear = world.createJoint(rdef) as RevoluteJoint
        rdef.localAnchorB.set(0f, legHH)

        rdef.bodyB = legUpperFront
        rdef.localAnchorA.set(torsoHW, torsoHH-0.05f)
        jointHipFront = world.createJoint(rdef) as RevoluteJoint

        rdef.bodyA = legUpperFront
        rdef.bodyB = legLowerFront
        rdef.localAnchorA.set(0f, -legHH) // local anchor B is already set to the top of the leg height
        jointKneeFront = world.createJoint(rdef) as RevoluteJoint
        rdef.bodyA = legUpperRear
        rdef.bodyB = legLowerRear
        rdef.localAnchorA.set(0f, -legHH-torsoHH*0.5f) // local anchor B is already set to the top of the leg height
        jointKneeRear = world.createJoint(rdef) as RevoluteJoint

        jointHipRear.enableMotor(true)
        jointHipFront.enableMotor(true)
        jointHipRear.maxMotorTorque = jointHipMaxTorque*1.5f
        jointHipFront.maxMotorTorque = jointHipMaxTorque
        jointKneeFront.enableMotor(true)
        jointKneeRear.enableMotor(true)
        jointKneeFront.maxMotorTorque = jointKneeMaxTorque
        jointKneeRear.maxMotorTorque = jointKneeMaxTorque*1.5f

        jointHipFront.enableLimit(true)
        jointHipRear.enableLimit(true)
        jointKneeFront.enableLimit(true)
        jointKneeRear.enableLimit(true)
        jointHipRear.setLimits(-85f.toRadians(), 85f.toRadians())
        jointHipFront.setLimits(-85f.toRadians(), 85f.toRadians())
        jointKneeRear.setLimits(5f.toRadians(), 175f.toRadians())
        jointKneeFront.setLimits(5f.toRadians(), 175f.toRadians())

        aiHipRear = DeepQLearner(getCurrentState().size, numberOfActions, hiddenLayerConfig, replayMemoryCapacity, explorationLength)
        aiHipFront = DeepQLearner(getCurrentState().size, numberOfActions, hiddenLayerConfig, replayMemoryCapacity, explorationLength)
        aiKneeRear = DeepQLearner(getCurrentState().size, numberOfActions, hiddenLayerConfig, replayMemoryCapacity, explorationLength)
        aiKneeFront = DeepQLearner(getCurrentState().size, numberOfActions, hiddenLayerConfig, replayMemoryCapacity, explorationLength)
        aiHipRear.name = "Hip-rear"
        aiHipFront.name = "Hip-front"
        aiKneeRear.name = "Knee-rear"
        aiKneeFront.name = "Knee-front"

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
            legLowerFront.linearVelocity.y - torso.linearVelocity.y,
            footFrontContacts.toF(),
            footRearContacts.toF())

    internal fun update(dt: Float) {

        if (!isAutonomousEnabled && Gdx.input.isKeyJustPressed(Keys.Q)) isAutonomousEnabled = true
        else if (Gdx.input.isKeyJustPressed(Keys.Q)) isAutonomousEnabled = false
        if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) && Gdx.input.isKeyJustPressed(Keys.S)) {
            if (isOverwritingPreviousSave) isOverwritingPreviousSave = false
            else isOverwritingPreviousSave = true
        }

        if (Gdx.input.isKeyJustPressed(Keys.PLUS)) {
            aiHipFront.neuralNetwork.loadSaveState(Gdx.files.local("testing/neural_nets/ai_hip_front.txt").readString())
            aiHipRear.neuralNetwork.loadSaveState(Gdx.files.local("testing/neural_nets/ai_hip_rear.txt").readString())
            aiKneeFront.neuralNetwork.loadSaveState(Gdx.files.local("testing/neural_nets/ai_knee_front.txt").readString())
            aiKneeRear.neuralNetwork.loadSaveState(Gdx.files.local("testing/neural_nets/ai_knee_rear.txt").readString())
        }

        if (!isFastForwarding && Gdx.input.isKeyJustPressed(Keys.T)) isFastForwarding = true
        else if (Gdx.input.isKeyJustPressed(Keys.T)) isFastForwarding = false

        if (!isDebugging && Gdx.input.isKeyJustPressed(Keys.GRAVE)) {
            isDebugging = true
            aiHipFront.isDebugEnabled = true
            aiHipRear.isDebugEnabled = true
            aiKneeFront.isDebugEnabled = true
            aiKneeRear.isDebugEnabled = true
        }
        else if (Gdx.input.isKeyJustPressed(Keys.GRAVE)) {
            isDebugging = false
            aiHipFront.isDebugEnabled = false
            aiHipRear.isDebugEnabled = false
            aiKneeFront.isDebugEnabled = false
            aiKneeRear.isDebugEnabled = false
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT) && explorationProbability < 0.9f) explorationProbability += 0.1f
        else if (Gdx.input.isKeyJustPressed(Keys.LEFT) && explorationProbability > 0.1f) explorationProbability -= 0.1f
        //if (Gdx.input.isKeyJustPressed(Keys.UP) && explorationProbability)

        if (Gdx.input.isKeyPressed(Keys.PERIOD)) playbackSpeed++
        else if (Gdx.input.isKeyPressed(Keys.COMMA) && playbackSpeed != 1) playbackSpeed--

        if (!isFastForwarding) stepSimulation(dt)
        else for (i in 1..playbackSpeed) stepSimulation(1/30f)

        if (isExplorationEnabled && Gdx.input.isKeyJustPressed(Keys.E)) isExplorationEnabled = false
        else if (Gdx.input.isKeyJustPressed(Keys.E)) isExplorationEnabled = true

        writeTime += dt
    }

    var writeTime = 0f
    fun stepSimulation(dt: Float) {
        timeLeftUntilNextAction -= dt

        if (timeLeftUntilNextAction <= 0) {

            if (isAutonomousEnabled) {
                updateReward()
                command(aiHipRear.updateStateAndRewardThenSelectAction(getCurrentState(), currentReward, isExplorationEnabled, explorationProbability), jointHipRear)
                command(aiHipFront.updateStateAndRewardThenSelectAction(getCurrentState(), currentReward, isExplorationEnabled, explorationProbability), jointHipFront)
                command(aiKneeRear.updateStateAndRewardThenSelectAction(getCurrentState(), currentReward, isExplorationEnabled, explorationProbability), jointKneeRear)
                command(aiKneeFront.updateStateAndRewardThenSelectAction(getCurrentState(), currentReward, isExplorationEnabled, explorationProbability), jointKneeFront)
                //printMatrix(Array(1, {getCurrentState()}))
                    for (i in 1..6) {
                        aiHipRear.trainFromReplayMemory(minibatchSize, learningRate, weightDecay, discountFac)
                        aiHipFront.trainFromReplayMemory(minibatchSize, learningRate, weightDecay, discountFac)
                        aiKneeRear.trainFromReplayMemory(minibatchSize, learningRate, weightDecay, discountFac)
                        aiKneeFront.trainFromReplayMemory(minibatchSize, learningRate, weightDecay, discountFac)
                    }
                    cost = (aiHipFront.neuralNetwork.cost + aiHipRear.neuralNetwork.cost + aiKneeFront.neuralNetwork.cost + aiKneeRear.neuralNetwork.cost) / 4f
                    if (isOverwritingPreviousSave && writeTime > 3f) {
                        try {
                            Gdx.files.local("testing/neural_nets/ai_hip_front.txt").writeString(aiHipFront.neuralNetwork.getSaveState(), false)
                            Gdx.files.local("testing/neural_nets/ai_hip_rear.txt").writeString(aiHipRear.neuralNetwork.getSaveState(), false)
                            Gdx.files.local("testing/neural_nets/ai_knee_front.txt").writeString(aiKneeFront.neuralNetwork.getSaveState(), false)
                            Gdx.files.local("testing/neural_nets/ai_knee_rear.txt").writeString(aiKneeRear.neuralNetwork.getSaveState(), false)
                        } catch (e: Exception) {
                            println("Error saving file: ${e.localizedMessage}")
                        }
                        writeTime = 0f
                    //println("END COST = " + cost)
                }
            } else {
                updateReward()
                if (Gdx.input.isKeyPressed(Keys.D)) jointHipRear.motorSpeed = jointSpeed
                else if (Gdx.input.isKeyPressed(Keys.F)) jointHipRear.motorSpeed = -jointSpeed
                else jointHipRear.motorSpeed = 0f
                if (Gdx.input.isKeyPressed(Keys.J)) jointHipFront.motorSpeed = jointSpeed
                else if (Gdx.input.isKeyPressed(Keys.K)) jointHipFront.motorSpeed = -jointSpeed
                else jointHipFront.motorSpeed = 0f
                if (Gdx.input.isKeyPressed(Keys.C)) jointKneeRear.motorSpeed = jointSpeed
                else if (Gdx.input.isKeyPressed(Keys.V)) jointKneeRear.motorSpeed = -jointSpeed
                else jointKneeRear.motorSpeed = 0f
                if (Gdx.input.isKeyPressed(Keys.N)) jointKneeFront.motorSpeed = jointSpeed
                else if (Gdx.input.isKeyPressed(Keys.M)) jointKneeFront.motorSpeed = -jointSpeed
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

    var footRearContacts = 0
    var footFrontContacts = 0
    var footRearContactPoint = Vector2(0f,0f)
    var footFrontContactPoint = Vector2(0f,0f)
    fun isFootRearContacting(): Boolean = footRearContacts > 0
    fun isFootFrontContacting(): Boolean = footFrontContacts > 0


    inner class BodyContactListener : ContactListener {
        private var fa: Fixture? = null
        private var fb: Fixture? = null

        override fun beginContact(contact: Contact) {

            fa = contact.fixtureA
            fb = contact.fixtureB

            if (isContacting("footRear", "ground")) {
                footRearContactPoint = contact.worldManifold.points.first()
                footRearContacts++
            }
            if (isContacting("footFront", "ground")) {
                footFrontContactPoint = contact.worldManifold.points.first()
                footFrontContacts++
            }

        }

        override fun endContact(contact: Contact) {

            fa = contact.fixtureA
            fb = contact.fixtureB

            if (isContacting("footRear", "ground")) footRearContacts--
            if (isContacting("footFront", "ground")) footFrontContacts--

        }

        override fun preSolve(contact: Contact, oldManifold: Manifold) {}

        override fun postSolve(contact: Contact, impulse: ContactImpulse) {}

        private fun isContacting(f1: String, f2: String): Boolean {
            if (fa!!.userData === f1 && fb!!.userData === f2) return true
            else if (fa!!.userData === f2 && fb!!.userData === f1) return true
            else return false
        }
    }

    fun reduceIntervalRadians(ang: Float): Float {
        var theta = ang
        while (theta < 0) theta += 2f*Math.PI.toF()
        while (theta > 2*Math.PI) theta -= 2f*Math.PI.toF()
        return theta
    }
    fun updateReward() {
        currentReward = 0.3f * torso.linearVelocity.x
        if (currentReward < 0) currentReward = 0f
        currentReward -= 0.5f // punishment for being idle
        val angle = reduceIntervalRadians(torso.angle).toDegrees()
        if (angle > 160f && angle < 200f) {
            if (isFootFrontContacting() || isFootRearContacting()) {
                torso.applyForce(Vector2(0f, 500f), Vector2(torso.position.x + torsoHW * 10, torso.position.y), true)
                torso.applyForce(Vector2(0f, -100f), Vector2(torso.position.x - torsoHW, torso.position.y), true)//Torque(12000f, true)
            }
            currentReward -= 0.2f
        }
    }

    fun getTimePassed(): String {
        val minutes = (secondsPassed/60f).toInt().toString()
        var seconds = (((secondsPassed/60f) - (secondsPassed/60f).toInt())*60).toInt().toString()
        if (seconds.length == 1) seconds = "0" + seconds
        return minutes + ":" + seconds
    }

    private fun getActualPlaybackSpeed(): Float = Math.round(playbackSpeed * 2f * Gdx.graphics.framesPerSecond / 60f * 100f) / 100f


    override fun render(dt: Float) {
        update(dt)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        game.shapeRender.begin(ShapeRenderer.ShapeType.Filled)

        game.shapeRender.setColor(0.1f,0.1f,0.1f,1f)
        for (i in 0..3000) game.shapeRender.rect(i*game.width, 0f, game.width*(1/4f), game.height)
        for (i in 0..3000) game.shapeRender.rect(-i*game.width, 0f, game.width*(1/4f), game.height)

        game.shapeRender.color = Color.YELLOW
        if (isFootFrontContacting()) game.shapeRender.circle(footFrontContactPoint.x *ppm, footFrontContactPoint.y *ppm, 8f)
        game.shapeRender.color = Color.RED
        if (isFootRearContacting()) game.shapeRender.circle(footRearContactPoint.x *ppm, footRearContactPoint.y *ppm, 8f)

        game.shapeRender.end()

        debugRender.render(world, game.phys2DCam.combined)

        game.batch.begin()
        game.font.draw(game.batch, "" + Gdx.graphics.framesPerSecond, game.width * 0.27f, game.height * 0.85f)
        game.font.draw(game.batch, getTimePassed(), 70f, game.height/2f)
        if (isFastForwarding) game.font.draw(game.batch, "Playback Rate: ${2f*playbackSpeed} (${getActualPlaybackSpeed()})" , 20f, game.height - 50f)
        game.font.draw(game.batch, "R = " + Math.round(currentReward*1000f)/1000f, game.width * 0.15f, game.height * 0.1f)
        game.font.draw(game.batch, "Velocity = " + Math.round(torso.linearVelocity.x*1000f)/1000f, game.width * 0.05f, game.height * 0.1f+70f)
        game.font.draw(game.batch, "Cost = " + Math.round(cost*1000f)/1000f, game.width * 0.1f, game.height * 0.3f)
        game.font.draw(game.batch, "Foot Rear: $footRearContacts", game.width * 0.1f, game.height * 0.6f +40f)
        game.font.draw(game.batch, "Foot Front: $footFrontContacts", game.width * 0.1f, game.height * 0.6f -40f)

        game.font.color = Color.GREEN
        if (isOverwritingPreviousSave)
           game.font.draw(game.batch, "# SAVING DATA #", game.width - 750, game.height - 40)
        if (isExplorationEnabled) {
            game.font.draw(game.batch, "EXPLORATION LEARNING ENABLED", game.width - 1500f, game.height - 340f)
            game.font.draw(game.batch, "Exploration Probability: " + Math.round(explorationProbability * 100f) / 100f, game.width - 1400f, game.height - 420f)
        }
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