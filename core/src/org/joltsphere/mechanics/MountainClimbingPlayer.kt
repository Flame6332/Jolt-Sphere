package org.joltsphere.mechanics

import org.joltsphere.main.JoltSphereMain
import org.joltsphere.misc.ObjectData

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.World

class MountainClimbingPlayer(private val world: World, x: Float, y: Float, private val color: Color) {
    var body: Body
    private val bdef: BodyDef
    private val fdef: FixtureDef
    private val circle: CircleShape
    private var isGrounded = false
    private var canDouble = false

    private val ppm = JoltSphereMain.ppm
    private var dt = 0.01666f

    init {

        bdef = BodyDef()
        bdef.type = BodyType.DynamicBody
        bdef.position.set(x / ppm, y / ppm)
        bdef.fixedRotation = false
        bdef.bullet = true
        bdef.linearDamping = 0.2f
        bdef.angularDamping = 0.5f
        body = world.createBody(bdef)
        body.userData = ObjectData("mountainClimber")
        fdef = FixtureDef()
        circle = CircleShape()
        circle.radius = 50 / ppm
        fdef.shape = circle
        fdef.friction = 0.1f
        fdef.density = 10f
        fdef.restitution = 0f
        body.createFixture(fdef)

    }

    fun shapeRender(shapeRender: ShapeRenderer) {
        shapeRender.color = color
        shapeRender.circle(body.position.x * ppm, body.position.y * ppm, 50f)
        shapeRender.color = Color.GOLD
    }

    fun update(dt: Float, groundContacts: Int) {
        this.dt = dt

        if (groundContacts > 0)
            isGrounded = true
        else
            isGrounded = false

        if (isGrounded) {
            canDouble = true
        }
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
        }
        if (canDouble) {
            canDouble = false
            body.setLinearVelocity(0f, 0f)
            body.applyLinearImpulse(Vector2(0f, 12f), body.position, true)
        }
        //body.applyForceToCenter(0, 40, true);
    }

    fun fly() {
        body.applyForceToCenter(Vector2(0f, 60f), true)
    }

    fun input(up: Int, down: Int, left: Int, right: Int, modifier: Int, fly: Boolean) {
        if (Gdx.input.isKeyPressed(left)) moveLeft()
        if (Gdx.input.isKeyPressed(right)) moveRight()
        if (Gdx.input.isKeyPressed(up) && fly)
            fly()
        else if (Gdx.input.isKeyJustPressed(up)) jump()
    }

}
