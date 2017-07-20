package org.joltsphere.mechanics

import org.joltsphere.main.JoltSphereMain
import org.joltsphere.misc.Misc
import org.joltsphere.misc.ObjectData

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.World

open class StreamBeamPlayer(private val world: World, x: Float, y: Float, private val color: Color) {
    lateinit var body: Body
    private lateinit var sightBody: Body
    private lateinit var rotatingBody: Body
    private lateinit var firedBalls: Array<FiredBall>
    private lateinit var bdef: BodyDef
    private lateinit var bdefFire: BodyDef
    private lateinit var fdef: FixtureDef
    private lateinit var fdefFire: FixtureDef
    private lateinit var circle: CircleShape
    private lateinit var sightCircle: CircleShape
    open fun density(): Float {
        return 10f
    }

    private var isGrounded = false
    private var canDoubleJump = false

    private val firingRate = 0.016667f // the smaller the faster
    private var ballsTryingToBeFired = 0f
    private var firedBallCount = 0
    var firedBallsToBeRemoved: Array<Int>
    private var canFire = true
    private var energyLevel = 0f
    private val maxEnergy = 100f

    private var sightRotationAmount = 0f
    private var didRotateLastFrame = false
    private val rotationSpeed = 0.005f // beginning rotation speed
    private var currentRotationSpeed = rotationSpeed

    private val ppm = JoltSphereMain.ppm
    private var dt = 0.01666f

    init {

        createObjects(x, y)
        firedBallsToBeRemoved = Array<Int>()

    }

    fun shapeRender(shapeRender: ShapeRenderer) {
        shapeRender.color = color
        shapeRender.circle(body.position.x * ppm, body.position.y * ppm, 50f)
        shapeRender.color = Color.GOLD
        for (i in 0..firedBalls.size - 1) {
            shapeRender.circle(firedBalls.get(i).fireBody.position.x * ppm, firedBalls.get(i).fireBody.position.y * ppm, 16f)
        }
        shapeRender.color = Color.WHITE
        shapeRender.circle(sightBody.position.x * ppm, sightBody.position.y * ppm, 20f)

        shapeRender.color = Color.WHITE
        shapeRender.rect(0f, body.position.y * ppm, maxEnergy * 5 + 30, 100f)

        shapeRender.color = Color.YELLOW
        shapeRender.rect(30f, body.position.y * ppm + 15, energyLevel * 5, 70f)
    }

    fun update(dt: Float, groundContacts: Int) {
        this.dt = dt
        updateFiredBalls()

        if (groundContacts > 0)
            isGrounded = true
        else
            isGrounded = false

        if (isGrounded) canDoubleJump = true

        if (energyLevel > 0) energyLevel -= 0.125f
    }

    fun moveLeft() {
        moveHorizontal(-1)
    }

    fun moveRight() {
        moveHorizontal(1)
    }

    private fun moveHorizontal(dir: Int) {
        body.applyForceToCenter((30 * dir).toFloat(), 0f, true)
        body.applyTorque((-5 * dir).toFloat(), true)
    }

    fun jump() {
        if (isGrounded) {
            body.setLinearVelocity(body.linearVelocity.x * 0.3f, body.linearVelocity.y * 0.3f)
            body.applyLinearImpulse(Vector2(0f, 16f), body.position, true)
        } else if (canDoubleJump) {
            canDoubleJump = false
            body.setLinearVelocity(0f, 0f)
            body.applyLinearImpulse(Vector2(0f, 12f), body.position, true)
        }
        //body.applyForceToCenter(0, 40, true);
    }

    fun rotateAimLeft() {
        rotateAim(-1)
    }

    fun rotateAimRight() {
        rotateAim(1)
    }

    private fun rotateAim(dir: Int) {
        didRotateLastFrame = true
        sightRotationAmount -= currentRotationSpeed * dir.toFloat() * dt / 0.016666f
        rotatingBody.setTransform(rotatingBody.position, sightRotationAmount)
        if (currentRotationSpeed < 0.15f)
            currentRotationSpeed += (0.011 * dt / 0.0166666f).toFloat()
    }

    fun notRotating() {
        if (didRotateLastFrame) {
            didRotateLastFrame = false
            currentRotationSpeed = rotationSpeed
        }
    }

    fun fire() {
        if (energyLevel >= maxEnergy)
            canFire = false
        else
            canFire = true
        if (canFire) {
            energyLevel += 1f
            ballsTryingToBeFired += dt / firingRate
            val ballsToFireThisFrame = ballsTryingToBeFired.toInt()
            for (i in 1..ballsToFireThisFrame) {
                bdefFire.position.set(sightBody.position)
                firedBalls.add(FiredBall())
                ballsTryingToBeFired-- // shot a ball
            }
        }
    }

    private fun updateFiredBalls() {
        for (firedBall in firedBalls) {
            firedBall.update()
            if (firedBall.isDead) firedBalls.removeValue(firedBall, true)
        }
    }

    private inner class FiredBall {
        val fireBody: Body
        private var deathCountdown = 1.0f // how long they last in seconds
        var isDead = false
        private val count: Int

        init {
            fireBody = world.createBody(bdefFire)
            firedBallCount++
            count = firedBallCount
            fireBody.userData = ObjectData(firedBallCount, "fire")
            fireBody.createFixture(fdefFire)
            val fireVector = Misc.vectorComponent(body.position, sightBody.position, 8f)

            fireBody.applyLinearImpulse(fireVector, fireBody.position, true)
            // there's a reason that we didn't add recoil :(
            //body.applyLinearImpulse(-fireVector.x, -fireVector.y, body.getPosition().x, body.getPosition().y, true);
            if (density() > 1) body.applyLinearImpulse(-fireVector.x * 0.07f, -fireVector.y * 0.07f, body.position.x, body.position.y, true)
        }

        fun update() {
            var shouldDestroy = false
            for (i in firedBallsToBeRemoved) {
                if (i.toInt() == count) shouldDestroy = true
                //System.out.println(i + " " + ((ObjectData)fireBody.getUserData()).count);
            }
            if (deathCountdown < 0 || shouldDestroy) {
                firedBallsToBeRemoved.removeValue(count, true)
                world.destroyBody(fireBody)
                isDead = true
            }
            /*if (shouldDestroy) {
				firedBallsToBeRemoved.removeValue(count, true);
				if (fireBody.getFixtureList().size > 0) fireBody.getFixtureList().first().setSensor(true);
			}*/
            deathCountdown -= dt
        }
    }

    fun input(up: Int, down: Int, left: Int, right: Int, rotateLeft: Int, rotateRight: Int, fire: Int) {
        if (Gdx.input.isKeyPressed(left)) moveLeft()
        if (Gdx.input.isKeyPressed(right)) moveRight()
        if (Gdx.input.isKeyJustPressed(up)) jump()
        if (Gdx.input.isKeyPressed(rotateLeft))
            rotateAimLeft()
        else if (Gdx.input.isKeyPressed(rotateRight))
            rotateAimRight()
        else
            notRotating()
        if (Gdx.input.isKeyPressed(fire)) fire()
    }

    private fun createObjects(x: Float, y: Float) {

        bdef = BodyDef()
        bdef.type = BodyType.DynamicBody
        bdef.position.set(x / ppm, y / ppm)
        bdef.fixedRotation = false
        bdef.bullet = true
        bdef.linearDamping = 0.2f
        bdef.angularDamping = 0.5f
        body = world.createBody(bdef)
        body.userData = ObjectData("streamBeam")
        fdef = FixtureDef()
        circle = CircleShape()
        circle.radius = 50 / ppm
        fdef.shape = circle
        fdef.friction = 0.1f
        fdef.density = density()
        fdef.restitution = 0f
        fdef.filter.categoryBits = 1
        fdef.filter.maskBits = 1
        body.createFixture(fdef)

        bdef = BodyDef()
        bdef.type = BodyType.DynamicBody
        bdef.position.set(x / ppm, y / ppm)
        bdef.fixedRotation = true
        bdef.bullet = false
        rotatingBody = world.createBody(bdef)
        sightBody = world.createBody(bdef)
        fdef = FixtureDef()
        sightCircle = CircleShape()
        sightCircle.radius = 20 / ppm
        fdef.friction = 0f
        fdef.density = 0.01f
        fdef.restitution = 0f
        fdef.filter.categoryBits = 2
        fdef.filter.maskBits = 1
        fdef.shape = circle
        rotatingBody.createFixture(fdef)
        fdef.shape = sightCircle
        sightBody.createFixture(fdef)

        val wdef = RevoluteJointDef()
        wdef.bodyA = rotatingBody
        wdef.bodyB = sightBody
        wdef.localAnchorA.set(50 / ppm, 0f)
        wdef.localAnchorB.set(0f, 0f)
        wdef.collideConnected = false
        world.createJoint(wdef)
        val rdef = RevoluteJointDef()
        rdef.bodyA = body
        rdef.bodyB = rotatingBody
        rdef.localAnchorA.set(0f, 0f)
        rdef.localAnchorB.set(0f, 0f)
        rdef.collideConnected = false
        world.createJoint(rdef)

        firedBalls = Array<FiredBall>()

        bdefFire = BodyDef()
        bdefFire.type = BodyType.DynamicBody
        bdefFire.fixedRotation = false
        bdefFire.bullet = false
        fdefFire = FixtureDef()
        val fireCircle = CircleShape()
        fireCircle.radius = 16 / ppm
        fdefFire.shape = fireCircle
        fdefFire.friction = 1f
        fdefFire.density = 10f
        fdefFire.restitution = 0.3f
        fdefFire.filter.categoryBits = 1
        fdefFire.filter.maskBits = 1
    }

}
