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
import org.joltsphere.misc.LastFrame
import org.joltsphere.misc.Misc

class ArenaPlayer(xpos: Int, ypos: Int, var world: World, var player: Int, var color: Color) {

    lateinit var event: ArenaPlayerEventListener
    var ppm = JoltSphereMain.ppm

    lateinit var body: Body
    lateinit var fixture: Fixture
    var circShape: CircleShape = CircleShape()
    var jumpShape: CircleShape = CircleShape()
    //var smashShape: PolygonShape = PolygonShape()
    var locationIndicator: Vector2 = Vector2()
    val startingLocation: Vector2 = Vector2(xpos.toFloat(), ypos.toFloat())
    var trail: Array<Vector2>
    var paint: Array<Vector2>

    lateinit var fdefBall: FixtureDef
    lateinit var fdefSmash: FixtureDef
    lateinit var fdefSmashJump: FixtureDef

    var knockouts = 0
    private var dt: Float = 1 / 60f // default value

    var smashRestitution: Float = 0f // restitution of smash object
    var smashDensity: Float = 80f
    var smashJumpRestitution: Float = 0.6f
    var smashJumpDensity: Float = 375f / 4f

    var energyTimerSpeed = 1 / 50f
    var energyTimer = 1f
    var minimumEnergy = 0.05f

    var currentRecievingSmashRestitution = 0f // restitution whenever player hit with a smash
    var recievingSmashRestitution = 0.4f
    var beforeContactRestitution = 0f
    var maximumContactRestitution = 1 / minimumEnergy * recievingSmashRestitution

    var jumpDelay = 7f
    var jumpTimer = jumpDelay

    var jumpHoldPhase = 15f //half jump time
    var jumpHoldTimer = jumpHoldPhase

    var smashLength = 60f
    var smashTimer = smashLength

    private val magnifyLength = 120f
    private var magnifyTimer = magnifyLength

    var attackCooldownLength = 4.16666f // in seconds
    var attackCooldown = 0f

    var smashJumpLength = 17f // length of jump
    var smashJumpPeriodLength = 40f // period of time able to jump
    var smashJumpPeriod = smashJumpPeriodLength

    var arenaSpace: Float = 0f
    private var indicatorScl = 1f // place holder value of 1
    private val indicatorSclLimit = 0.01f
    var indicatorSize = 1f

    init {

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

        //updatePaint() // uncomment this code to paint
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
        if (contact > 0) { // if on ground
            hasDoubled = false // reset double jump
            canJump = true // canJump is the same as isGrounded except that it stays true for a short period of time even after leaving the ground
            jumpTimer = jumpDelay // continuously keep the timer maxed when making contact with the ground
            isGrounded = true
        }
        else isGrounded = false
    }

    val position: Vector2
        get() = Vector2(body.position.x * ppm, body.position.y * ppm)


    /* !!!!! MOVEMENT !!!!! */

    var hasDoubled: Boolean = true
    var canJump: Boolean = false // is true when on ground and for a short delay after leaving the ground
    var canJumpHold: Boolean = false

    fun moveLeft(percent: Float) = moveHorizontal(-1f, percent)
    fun moveRight(percent: Float) = moveHorizontal(1f, percent)
    private fun moveHorizontal(dir: Float, percent: Float) {
        if (isSmashing) { // smashing
            if (isGrounded) {
                body.applyForceToCenter(50000f * dir * 0.01666666f, 0f, true)
                body.applyTorque(-100f * dir, true)
            } else { // air smashing
                body.applyForceToCenter(60000f * dir * 0.01666666f, 0f, true)
                body.applyTorque(-100f * dir, true)
            }
        } else { // not smashing
            if (isGrounded) {
                body.applyTorque(-12f * dir, true)
                body.applyForceToCenter(1200f * dir * 0.01666666f, 0f, true)
            } else {
                body.applyForceToCenter(900f * dir * 0.01666666f, 0f, true)
            }
        }
    }
    fun jump() {
        if (canJump) { // if on ground
            if (canSmashJump)
                smashJump()
            else {
                body.setLinearVelocity(body.linearVelocity.x * 0.3f, body.linearVelocity.y * 0.3f)
                body.applyLinearImpulse(Vector2(0f, 280f * 0.016666666f), body.position, true)
                jumpHoldTimer = jumpHoldPhase
            }
        } else if (!hasDoubled) { // if in air and hasn't doubled
            body.angularVelocity = 0f
            body.setLinearVelocity(0f, 0f)
            body.applyLinearImpulse(Vector2(0f, 310f * 0.01666666f), body.position, true)
            hasDoubled = true
        }
    }
    fun jumpHold() {
        if (!hasDoubled && canJumpHold) {
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
            canJumpHold = true
            jumpHoldTimer -= 60 * dt
        } else
            canJumpHold = false
    }



    /* !!!!! ATTACKS !!!!! */

    var isSmashing: Boolean = false
    var isSmashJumping: Boolean = false
    var isMagnifying: Boolean = false

    var canSmash: Boolean = false
    var canSmashJump: Boolean = false
    var isGrounded: Boolean = false

    val isAttacking: Boolean
        get() {
            return isSmashing || isMagnifying || isSmashJumping
        }

    private fun updateAttackCooldown(dt: Float) {
        if (!isAttacking) {
            if (attackCooldown > attackCooldownLength)
                canSmash = true
            else
                attackCooldown += dt
        }
    }

    var wasSmashing: LastFrame = LastFrame()

    // TODO tune upsmash because it is overpowered
    /*
    SMASH (starts sequence), at end of smash ->
        if up clicked --- in air -> double jump detachment smash ::: if down pressed -> meteor shower
                      --- on ground -> up smash
         if down clicked again -> super smash

         two ways to end an attack: out of time |or| premature end
     */

    fun smash() {
        if (canSmash) {
            if (smashTimer == smashLength) smashBegin() // timer has not been changed yet, so begin smash
            if (!isGrounded) body.applyForceToCenter(0f, -30000 * 0.01666666f, true) // if not grounded, down force
            if (canJump) body.applyForceToCenter(0f, -30000 * 0.0166666f, true) // if within the jump delay, vaccum it had to the ground to prevent bouncing
            isSmashing = true
            wasSmashing.occured()
            smashTimer -= 60 * dt
            if (smashTimer < 0) smashEnded()
        }
    }

    fun notSmashing() { // called in keyboard controls when finger released
        if (wasSmashing.justEnded()) { // called if you smashed last frame
            smashJumpPeriod = smashJumpPeriodLength // gives you a smash jump period since you released before smash end
            if (canJump) canSmashJump = true // allows you to smash jump during smash
            smashEnded() // ends smash
        }
    }

    private fun smashBegin() {
        body.destroyFixture(fixture)
        fixture = body.createFixture(fdefSmash)
    }

    private fun smashEnded() {
        isSmashing = false
        smashTimer = smashLength
        canSmash = false
        attackCooldown = 0f
        body.destroyFixture(fixture)
        fixture = body.createFixture(fdefBall)
        beforeContactRestitution = fixture.restitution
    }

    private var wasHitBySmash: LastFrame = LastFrame()

    fun hitBySmash() { //called when get smashed
        wasHitBySmash.occured()
    }

    fun notHitBySmash(otherPlayer: ArenaPlayer) { // called whenever there isnt any contact with player in the update cycle
        if (wasHitBySmash.justEnded()) { // if hit by smash last frame
            val scale = 500f / maximumContactRestitution * currentRecievingSmashRestitution
            body.applyLinearImpulse(Vector2(otherPlayer.body.linearVelocity.x * scale * 0.01666666f, otherPlayer.body.linearVelocity.y * scale * 0.01666666f), body.position, true)
        }
    }
    private fun smashJump() {
        hasDoubled = true // so you cant double jump during smash jummp
        isSmashJumping = true

        body.destroyFixture(fixture)
        fixture = body.createFixture(fdefSmashJump)
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

    var wasMagnifying: LastFrame = LastFrame()

    fun magnify(otherPlayer: Vector2) {
        if (canSmash) {
            body.applyForce(Misc.vectorComponent(
                    body.position.x, body.position.y,
                    otherPlayer.x, otherPlayer.y,
                    100f), Vector2(0f, 0f), true)
            isMagnifying = true
            attackCooldown = 0f
            magnifyTimer -= 60 * dt
            if (magnifyTimer < 0) magnifyEnded()
            wasMagnifying.occured()
        }
    }
    fun notMagnifying() { // called every update that is not magnifying
        if (wasMagnifying.justEnded()) magnifyEnded()
    }

    private fun magnifyEnded() {
        isMagnifying = false
        magnifyTimer = magnifyLength
        canSmash = false
    }

    fun canMagnify(): Boolean {
        return canSmash
    }

    fun otherPlayerMagnified(otherPlayer: Vector2) {
        body.applyForce(Misc.vectorComponent(
                body.position.x, body.position.y,
                otherPlayer.x, otherPlayer.y,
                100f), Vector2(0f, 0f), true)/**/
    }

    fun knockedOut() {
        knockouts++ // adds to the number of times he's been knocked out
        resetEnergy() // sets energy to 0
        resetPlayer() // repositions the player
        event.playerKnockedOut(this) // triggers event in arenaspace to spread the message to other players
    }

    fun resetPlayer() {
        body.angularVelocity = 0f
        body.setLinearVelocity(0f, 0f)
        body.setTransform(startingLocation.x / ppm, startingLocation.y / ppm, 0f)
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

    var shouldLocationIndicate: Boolean = false

    internal fun updateLocationIndicator(width: Int, height: Int) {
        val w = width // shorten width
        val wMid = width / 2f // middle of width
        val h = height // shorten for height
        val hMid = height / 2f // middle of the height
        val x = body.position.x * ppm // position of player
        val y = body.position.y * ppm // position of player
        var xFin = 0f // final position for location indicator
        var yFin = 0f // final position for location indicator
        val cls = 0.02f // close
        val far = 1 - cls
        val xIn = (x > 0 && x < w)
        val yIn = (y > 0 && y < h)

        if (xIn && yIn)
            shouldLocationIndicate = false
        else { // set position of square
            shouldLocationIndicate = true
            if (!xIn && yIn) { // ball off edge
                yFin = y
                if (x < wMid) xFin = w * cls
                else xFin = w * far
            }
            else if (xIn && !yIn) { // ball above or below
                xFin = x
                if (y < hMid) yFin = w * cls
                else yFin = h - w * cls
            }
            else if (!xIn && !yIn) { // ball in corner
                if (x < wMid) xFin = w * cls // to left
                else xFin = w * far // to right
                if (y < hMid) yFin = w * cls // below
                else yFin = h - w * cls // above
            }
        }
        if (xFin < w * cls) xFin = w * cls
        if (xFin > w * far) xFin = w * far
        if (yFin < w * cls) yFin = w * cls
        if (yFin > h - w * cls) yFin = h - w * cls

        locationIndicator.x = xFin
        locationIndicator.y = yFin

        if (shouldLocationIndicate) { // set scale of indicator if indicator
            val rng = 1 - indicatorSclLimit //range
            val shrnk = rng / arenaSpace // shrink

            if (!xIn && yIn) { // ball off edge
                if (x < wMid) indicatorScl = 1 - -1 * x * shrnk
                else indicatorScl = 1 - (x - w) * shrnk
            }
            else if (xIn && !yIn) { // ball above or below
                if (y < hMid) indicatorScl = 1 - -1 * y * shrnk
                else indicatorScl = 1 - (y - h) * shrnk
            }
            else if (!xIn && !yIn) { // ball in corner
                val tempX: Float
                val tempY: Float
                if (x < wMid) tempX = 1 - -1 * x * shrnk // to left
                    else tempX = 1 - (x - w) * shrnk // to right
                if (y < hMid) tempY = 1 - -1 * y * shrnk // below
                    else tempY = 1 - (y - h) * shrnk // above
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
        //if (x < -arenaSpace || x > (w + arenaSpace)) knocked out();
        if (y < -arenaSpace || y > h + arenaSpace) knockedOut()
    }

    internal fun createBall(xpos: Int, ypos: Int) {
        val bdef = BodyDef()
        bdef.type = BodyType.DynamicBody
        bdef.position.set(xpos / ppm, ypos / ppm)
        bdef.fixedRotation = false
        bdef.bullet = true
        body = world.createBody(bdef)
        body.userData = "p" + player
        fixture = body.createFixture(fdefBall)
    }

    internal fun createFixtureDefs() {
        circShape.radius = 26 / 100f
        jumpShape.radius = 60 / 100f

        fdefBall = FixtureDef()
        fdefBall.shape = circShape
        fdefBall.friction = 0.1f
        fdefBall.restitution = 0f
        fdefBall.density = 5f

        fdefSmash = FixtureDef()
        fdefSmash.shape = createSmashShape(1 / 1.2f)
        fdefSmash.friction = 0.2f
        fdefSmash.restitution = smashRestitution
        fdefSmash.density = smashDensity

        fdefSmashJump = FixtureDef()
        fdefSmashJump.shape = jumpShape//createSmashShape(2f);
        fdefSmashJump.friction = 0.5f
        fdefSmashJump.restitution = smashJumpRestitution
        fdefSmashJump.density = smashJumpDensity

    }
    fun createSmashShape(scl: Float): PolygonShape {
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