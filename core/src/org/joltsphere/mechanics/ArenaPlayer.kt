package org.joltsphere.mechanics

import org.joltsphere.main.JoltSphereMain

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array

class ArenaPlayer(xpos: Int, ypos: Int, var world: World, var player: Int, var color: Color) {


    var ppm = JoltSphereMain.ppm
    var FPSdt = 1f / JoltSphereMain.FPS

    lateinit var body: Body
    lateinit var fixture: Fixture
    var circShape: CircleShape
    var jumpShape: CircleShape
    lateinit var smashShape: PolygonShape
    var locationIndicator: Vector2
    var startingLocation: Vector2
    var trail: Array<Vector2>
    var paint: Array<Vector2>

    lateinit var fdefBall: FixtureDef
    lateinit var fdefSmash: FixtureDef
    lateinit var fdefSmashJump: FixtureDef

    var knockouts = 0
    private var dt: Float = 0.toFloat()

    var isSmashing = false
    var isSmashJumping = false
    var isMagnifying = false

    var hasDoubled = true
    var canJump = false
    var canHold = false
    var canAttack = false
    var canSmashJump = false
    var hadPreviouslySmashedLastFrame = false
    private var hadPreviouslyMagnifiedLastFrame = false
    var shouldLocationIndicate = false
    var isGrounded = false
    var wasKnockedOut = false

    var smashRestitution = 0f // restitution of smash object
    var smashDensity = 80f
    var smashJumpRestitution = 0.6f
    var smashJumpDensity = 375f / 4f

    var energyTimerSpeed = 1 / 50f
    var energyTimer = 1f
    var minimumEnergy = 0.05f

    var wasHitBySmash = false
    var currentRecievingSmashRestitution = 0f // restitution whenever player hit with a smash
    var recievingSmashRestitution = 0.4f
    var beforeContactRestitution = 0f
    var maximumContactRestitution = 1 / minimumEnergy * recievingSmashRestitution

    var jumpDelay = 7
    var jumpTimer = jumpDelay.toFloat()

    var jumpHoldPhase = 15f //half jump time
    var jumpHoldTimer = jumpHoldPhase

    var smashLength = 60f
    var smashTimer = smashLength

    private val magnifyLength = 120f
    private var magnifyTimer = magnifyLength

    var attackCooldownLength = 250f
    var attackCooldown = 0f

    var smashJumpLength = 17f //length of jump
    var smashJumpPeriodLength = 40f //period to jump
    var smashJumpPeriod = smashJumpPeriodLength

    var arenaSpace: Float = 0.toFloat()
    private var indicatorScl = 1f // place holder value of 1
    private val indicatorSclLimit = 0.01f
    var indicatorSize = 1f

    init {

        circShape = CircleShape()
        circShape.radius = 26 / 100f

        jumpShape = CircleShape()
        jumpShape.radius = 60 / 100f

        locationIndicator = Vector2()
        startingLocation = Vector2(xpos.toFloat(), ypos.toFloat())

        createFixtureDefs()

        createBall(xpos, ypos)

        trail = Array<Vector2>()
        paint = Array<Vector2>()

    }


    fun update(contact: Int, deltaTime: Float, width: Int, height: Int) {
        dt = deltaTime
        arenaSpace = 0.5f * height

        /* Basic Values if on the Ground */
        checkIfGrounded(contact)

        /* Creates timer to jump while bouncing around */
        updateJumpTimers(dt)

        /* Allows for smash jump after end of smash */
        updateSmashJump(dt)

        /* Sequence to preform if no longer smashing */
        updateAttackCooldown(dt)

        // Updates Indicator
        updateLocationIndicator(width, height)
        // Checks if Dead
        checkIfDead(width, height)

        updateEnergy()

        updatePaint()
    }


    fun shapeRender(sRender: ShapeRenderer) {

        sRender.color = color
        //for (int i = 1; i < trail.size; i++) {
        //sRender.circle(trail.get(i).x * ppm, trail.get(i).y * ppm, 50);
        //}

        /*if (shouldLocationIndicate) {
			float r = (indicatorSize / 2) *(1/indicatorSclLimit);
			sRender.rect(locationIndicator.x - r, locationIndicator.y - r, r*2, r*2);
			sRender.circle(locationIndicator.x, locationIndicator.y, r);
		}*/

        sRender.color = Color.GOLD
        if (!isSmashing && !isSmashJumping)
            sRender.circle(body.position.x * ppm, body.position.y * ppm, (circShape.radius * 100 + 1) * (ppm / 100f))

        if (shouldLocationIndicate) {
            //float r = (indicatorSize / 2) *(1/indicatorSclLimit);
            //sRender.rect(locationIndicator.x - r, locationIndicator.y - r, r*2, r*2);
            //sRender.circle(locationIndicator.x, locationIndicator.y, r);
        }


        sRender.color = color
        if (isSmashing)
            sRender.circle(body.position.x * ppm, body.position.y * ppm, 15 * (ppm / 100f))
        else if (isSmashJumping)
            sRender.circle(body.position.x * ppm, body.position.y * ppm, jumpShape.radius * 100f * (ppm / 100f))
        else
            sRender.circle(body.position.x * ppm, body.position.y * ppm, circShape.radius * 100f * (ppm / 100f))

        sRender.color = Color.WHITE
        if (shouldLocationIndicate) {
            val area = (Math.pow((indicatorSize / 2 * (1 / indicatorSclLimit)).toDouble(), 2.0) * Math.PI).toFloat()
            val r = (Math.pow(area * (1.01 - indicatorScl) / Math.PI,
                    0.5 /* square root */) + 2).toFloat()
            sRender.circle(locationIndicator.x, locationIndicator.y, r)
            //sRender.rect(locationIndicator.x - r, locationIndicator.y - r, r*2, r*2);
        }

    }

    fun renderPaint(shapeRender: ShapeRenderer, i: Int) {
        shapeRender.color = color
        shapeRender.circle(paint.get(i).x * ppm, paint.get(i).y * ppm, 50f)
    }

    private fun updatePaint() {
        paint.add(Vector2(body.position))
        if (paint.size >= 300) paint.removeIndex(0)
    }

    private fun checkIfGrounded(contact: Int) {
        if (contact > 0) {//if on ground
            hasDoubled = false //reset double jump
            canJump = true
            jumpTimer = jumpDelay.toFloat()
            isGrounded = true
        } else
            isGrounded = false
    }

    val position: Vector2
        get() = Vector2(body.position.x * ppm, body.position.y * ppm)

    fun moveLeft(percent: Float) {
        moveHorizontal(-1, percent)
    }

    fun moveRight(percent: Float) {
        moveHorizontal(1, percent)
    }

    private fun moveHorizontal(dir: Int, percent: Float) {
        if (isSmashing) { // smashing
            if (canJump) {
                body.applyForceToCenter(50000f * dir.toFloat() * 0.01666666f, 0f, true)
                body.applyTorque(-100f * dir, true)
            } else { // air smashing
                body.applyForceToCenter(60000f * dir.toFloat() * 0.01666666f, 0f, true)
                body.applyTorque(-100f * dir, true)
            }
        } else { // not smashing
            if (isGrounded) {
                body.applyTorque(-12f * dir, true)
                body.applyForceToCenter(1200f * dir.toFloat() * 0.01666666f, 0f, true)
            } else {
                body.applyForceToCenter(900f * dir.toFloat() * 0.01666666f, 0f, true)
            }
        }
    }


    fun jump() { //no delta because single impulse
        if (canJump) { //if on ground
            if (canSmashJump)
                smashJump()
            else {
                body.setLinearVelocity(body.linearVelocity.x * 0.3f, body.linearVelocity.y * 0.3f)
                body.applyLinearImpulse(Vector2(0f, 280f * 0.016666666f), body.position, true)
                jumpHoldTimer = jumpHoldPhase
            }
        } else if (!hasDoubled) {
            body.angularVelocity = 0f
            body.setLinearVelocity(0f, 0f)
            body.applyLinearImpulse(Vector2(0f, 310f * 0.01666666f), body.position, true)
            hasDoubled = true
        }
    }

    fun jumpHold() {
        if (!hasDoubled && canHold && !isSmashing) {
            body.applyForceToCenter(0f, 900 * 0.01666666f, true)
        }
    }

    private fun updateJumpTimers(dt: Float) {
        if (jumpTimer > 0) {
            canJump = true


            jumpTimer -= 60 * dt
        } else
            canJump = false
        //similar, except timer for held jumps
        if (jumpHoldTimer > 0) {
            canHold = true
            jumpHoldTimer -= 60 * dt
        } else
            canHold = false
    }

    private fun smashJump() {
        hasDoubled = true // so you cant double jump during smash jummp
        isSmashJumping = true

        body.destroyFixture(fixture)
        fixture = body.createFixture(fdefSmashJump)
        fixture.userData = "p" + player
        fixture.restitution = fixture.restitution + currentRecievingSmashRestitution / maximumContactRestitution

        smashJumpPeriod = smashJumpLength
        body.angularVelocity = body.angularVelocity * 0.3f // rotational speed decreased 30%
        body.setLinearVelocity(body.linearVelocity.x * 0.3f, body.linearVelocity.y * 0.1f) // velocities decreased 30% and 10%
        body.applyLinearImpulse(Vector2(0f, 2000000f * 0.0166666f / 16f), body.position, true)
    }

    private fun updateSmashJump(dt: Float) {
        if (smashJumpPeriod > 0) { // if you can still smash jump
            canSmashJump = true
            smashJumpPeriod -= 60 * dt
        } else if (canSmashJump) { // if youre out of time but the variable still there
            attackCooldown = 0f
            canSmashJump = false
            isSmashJumping = false
            body.setLinearVelocity(body.linearVelocity.x * 0.5f, body.linearVelocity.y * 0.05f) //not absolute stop
            smashEnded() // called in smash jump because it technically a smash by shape
        }
    }


    val isAttacking: Boolean
        get() {
            if (isSmashing || isMagnifying || isSmashJumping) {
                return true
            } else
                return false
        }

    private fun updateAttackCooldown(dt: Float) {
        if (!isAttacking) {
            if (attackCooldown > attackCooldownLength)
                canAttack = true
            else
                attackCooldown += 60 * dt
        }
    }

    fun smash() {
        if (canAttack) {
            if (smashTimer == smashLength) smashBegin() // timer has not been changed yet, so begin smash
            if (!isGrounded) body.applyForceToCenter(0f, -30000 * 0.01666666f, true)
            if (canJump) body.applyForceToCenter(0f, -30000 * 0.0166666f, true)
            isSmashing = true
            hadPreviouslySmashedLastFrame = true
            smashTimer -= 60 * dt
            if (smashTimer < 0) smashEnded()
        }
    }

    fun notSmashing() { // called in keyboard controls when finger released
        if (hadPreviouslySmashedLastFrame) { // called if you smashed last frame
            smashJumpPeriod = smashJumpPeriodLength // gives you a smash jump period since you released before smash end
            hadPreviouslySmashedLastFrame = false // updates this boolean
            if (canJump) canSmashJump = true // allows you to smash jump during smash
            smashEnded() // ends smash
        }
    }

    private fun smashBegin() {
        body.destroyFixture(fixture)
        fixture = body.createFixture(fdefSmash)
        fixture.userData = "p" + player
    }

    private fun smashEnded() {
        isSmashing = false
        smashTimer = smashLength
        canAttack = false
        attackCooldown = 0f
        body.destroyFixture(fixture)
        fixture = body.createFixture(fdefBall)
        fixture.userData = "p" + player
        beforeContactRestitution = fixture.restitution
    }

    fun hitBySmash() { //called when get smashed
        wasHitBySmash = true
    }

    fun notHitBySmash(otherPlayer: ArenaPlayer) { // called whenever there isnt any contact with player in the update cycle
        if (wasHitBySmash) {
            wasHitBySmash = false
            val scale = 500f / maximumContactRestitution * currentRecievingSmashRestitution
            body.applyLinearImpulse(Vector2(otherPlayer.body.linearVelocity.x * scale * 0.01666666f, otherPlayer.body.linearVelocity.y * scale * 0.01666666f), body.position, true)
        }
    }

    fun magnify(otherPlayer: Vector2) {
        if (canAttack) {
            body.applyForce(vectorComponent(
                    body.position.x, body.position.y,
                    otherPlayer.x, otherPlayer.y,
                    100f), Vector2(0f, 0f), true)
            isMagnifying = true
            attackCooldown = 0f
            magnifyTimer -= 60 * dt
            if (magnifyTimer < 0) magnifyEnded()
            hadPreviouslyMagnifiedLastFrame = true
        }
    }

    fun notMagnifying() {
        if (hadPreviouslyMagnifiedLastFrame) magnifyEnded()
    }

    private fun magnifyEnded() {
        isMagnifying = false
        hadPreviouslyMagnifiedLastFrame = false
        magnifyTimer = magnifyLength
        canAttack = false
    }

    fun canMagnify(): Boolean {
        if (canAttack)
            return true
        else
            return false
    }

    fun otherPlayerMagnified(otherPlayer: Vector2) {
        body.applyForce(vectorComponent(
                body.position.x, body.position.y,
                otherPlayer.x, otherPlayer.y,
                100f), Vector2(0f, 0f), true)/**/
    }

    fun knockedOut() {
        knockouts++

        body.angularVelocity = 0f
        body.setLinearVelocity(0f, 0f)
        body.setTransform(startingLocation.x / ppm, startingLocation.y / ppm, 0f)

        wasKnockedOut = true

        resetEnergy()
    }

    fun resetEnergy() {
        createFixtureDefs()
        energyTimer = 1f
    }

    fun otherPlayerKnockedOut() {
        resetEnergy()
    }

    internal fun updateEnergy() {
        if (energyTimer > minimumEnergy + energyTimerSpeed * dt)
        // add energy timer speed for going below minimum
            energyTimer -= energyTimerSpeed * dt // lower energy timer
        currentRecievingSmashRestitution = 1f / energyTimer * recievingSmashRestitution
    }

    fun contactingOtherPlayer(otherPlayer: ArenaPlayer) {
        if (otherPlayer.isSmashing) hitBySmash()
    }

    fun notContactingOtherPlayer(otherPlayer: ArenaPlayer) {
        notHitBySmash(otherPlayer)
    }

    fun vectorComponent(x1: Float, y1: Float, x2: Float, y2: Float, magnitude: Float): Vector2 {

        val xRelativeToFirst = x2 - x1
        val yRelativeToFirst = y2 - y1

        var pythagifiedLine = xRelativeToFirst * xRelativeToFirst + yRelativeToFirst * yRelativeToFirst

        pythagifiedLine = Math.sqrt(pythagifiedLine.toDouble()).toFloat()

        val percentOfLine = magnitude / pythagifiedLine

        val xIterationSpeed = percentOfLine * xRelativeToFirst
        val yIterationSpeed = percentOfLine * yRelativeToFirst

        return Vector2(xIterationSpeed, yIterationSpeed)
    }

    internal fun updateLocationIndicator(width: Int, height: Int) {
        val w = width
        val wMid = width / 2
        val h = height
        val hMid = height / 2

        val x = body.position.x * ppm
        val y = body.position.y * ppm

        var xFin = 0f
        var yFin = 0f

        val cls = 0.02f //close
        val far = 1 - cls

        val xin = if (x < 0 || x > w) false else true
        val yin = if (y < 0 || y > h) false else true

        if (xin && yin)
            shouldLocationIndicate = false
        else { // set position of square
            shouldLocationIndicate = true

            if (!xin && yin) { // ball off edge
                yFin = y
                if (x < wMid)
                    xFin = w * cls
                else
                    xFin = w * far
            } else if (xin && !yin) { // ball above or below
                xFin = x
                if (y < hMid)
                    yFin = w * cls
                else
                    yFin = h - w * cls
            } else if (!xin && !yin) { // ball in corner
                if (x < wMid)
                    xFin = w * cls // to left
                else
                    xFin = w * far // to right
                if (y < hMid)
                    yFin = w * cls // below
                else
                    yFin = h - w * cls // above
            }
        }

        if (xFin < w * cls) xFin = w * cls
        if (xFin > w * far) xFin = w * far
        if (yFin < w * cls) yFin = w * cls
        if (yFin > h - w * cls) yFin = h - w * cls

        locationIndicator.x = xFin
        locationIndicator.y = yFin

        if (!xin || !yin) { // set scale of square
            val rng = 1 - indicatorSclLimit //range
            val shrnk = rng / arenaSpace // shrink

            if (!xin && yin) { // ball off edge
                if (x < wMid)
                    indicatorScl = 1 - -1 * x * shrnk
                else
                    indicatorScl = 1 - (x - w) * shrnk
            } else if (xin && !yin) { // ball above or below
                if (y < hMid)
                    indicatorScl = 1 - -1 * y * shrnk
                else
                    indicatorScl = 1 - (y - h) * shrnk
            } else if (!xin && !yin) { // ball in corner
                val tempX: Float
                val tempY: Float
                if (x < wMid)
                    tempX = 1 - -1 * x * shrnk // to left
                else
                    tempX = 1 - (x - w) * shrnk // to right
                if (y < hMid)
                    tempY = 1 - -1 * y * shrnk // below
                else
                    tempY = 1 - (y - h) * shrnk // above

                if (tempX < tempY) indicatorScl = tempX else indicatorScl = tempY
                /*if (y < hMid) indicatorScl = 1 - ((-1*y) * shrnk); // below
				else indicatorScl = 1 - ((y - h) * shrnk); // above	*/

            }

        }

    }

    internal fun checkIfDead(width: Int, height: Int) {
        //int w = width;
        val h = height
        //float x = body.getPosition().x * ppm;
        val y = body.position.y * ppm

        //if (x < -arenaSpace || x > (w + arenaSpace)) knockedOut();
        if (y < -arenaSpace || y > h + arenaSpace) knockedOut()

    }


    internal fun createBall(xpos: Int, ypos: Int) {

        val bdef = BodyDef()
        bdef.type = BodyType.DynamicBody
        bdef.position.set(xpos / ppm, ypos / ppm)
        bdef.fixedRotation = false
        bdef.bullet = true

        body = world.createBody(bdef)

        fixture = body.createFixture(fdefBall)
        fixture.userData = "p" + player

    }

    internal fun createFixtureDefs() {

        fdefBall = FixtureDef()
        fdefBall.shape = circShape
        fdefBall.friction = 0.1f
        fdefBall.restitution = 0f
        fdefBall.density = 5f//(5 / 0.01666666f) * FPSdt;

        fdefSmash = FixtureDef()
        fdefSmash.shape = createSmashShape(1 / 1.2f)
        fdefSmash.friction = 0.2f
        fdefSmash.restitution = smashRestitution
        fdefSmash.density = smashDensity//(80 / 0.01666666f) * FPSdt;

        fdefSmashJump = FixtureDef()
        fdefSmashJump.shape = jumpShape//createSmashShape(2f);
        fdefSmashJump.friction = 0.5f
        fdefSmashJump.restitution = smashJumpRestitution
        fdefSmashJump.density = smashJumpDensity//(1500 / 0.01666666f) * FPSdt;

    }

    private fun createSmashShape(scl: Float): PolygonShape {

        val shape = PolygonShape()

        val v = arrayOfNulls<Vector2>(8)
        //bottom left
        v[0] = Vector2(-30 * scl / 100, -12.5f * scl / 100)
        v[1] = Vector2(-12.5f * scl / 100, -30 * scl / 100)

        //bottom right
        v[2] = Vector2(12.5f * scl / 100, -30 * scl / 100)
        v[3] = Vector2(30 * scl / 100, -12.5f * scl / 100)

        //top right
        v[4] = Vector2(30 * scl / 100, 12.5f * scl / 100)
        v[5] = Vector2(12.5f * scl / 100, 30 * scl / 100)

        //top left
        v[6] = Vector2(-12.5f * scl / 100, 30 * scl / 100)
        v[7] = Vector2(-30 * scl / 100, 12.5f * scl / 100)

        shape.set(v)

        return shape

    }

}
