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
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef

class Scene7(internal val game: JoltSphereMain) : Screen {

    internal var world: World
    internal var debugRender: Box2DDebugRenderer

    internal var ppm = JoltSphereMain.ppm
    internal var base: Body
    internal var stick: Body
    internal var joint: RevoluteJoint

    var isAutonomousEnabled = false
    val learnRate = 0.5f
    val discountFac = 1f

    // STATES (2^3 TOTAL)
    var horizontalStickSide = 0 // left is -1, right is 1
    var verticalStickSide = 0 // below is -1, above is 1
    var angularRotation = 0 // counterclockwise is -1, clockwise is 1
    // THE THREE POSSIBLE ACTIONS AT EACH TIMESTEP IS MOVE-LEFT, MOVE-RIGHT, OR DO-NOTHING
    var currentReward = 0f
    var actualAngle = 0f

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

        stick.applyLinearImpulse(Vector2(-10f, 0f), stick.localCenter, true)

    }

    internal fun update(dt: Float) {

        if (!isAutonomousEnabled && Gdx.input.isKeyJustPressed(Keys.Q)) isAutonomousEnabled = true

        if (isAutonomousEnabled) {
            moveOptimally(dt)
        }
        else {
            if (Gdx.input.isKeyPressed(Keys.LEFT)) moveLeft(dt)
            else if (Gdx.input.isKeyPressed(Keys.RIGHT)) moveRight(dt)
            else if (base.linearVelocity.x < -0.1f) moveRight(dt)
            else if (base.linearVelocity.x > 0.1f) moveLeft(dt)
            else base.setLinearVelocity(0f, base.linearVelocity.y)
        }

        updateRewardAndState();

    }

    fun updateRewardAndState() {
        // ANGLE = 0 is the upright postion of the stick and it increases to 360 degrees all the way around counterclockwise
        actualAngle = (joint.jointAngle*180f/Math.PI).toFloat() // convert radians to degrees
        while (actualAngle < 0) actualAngle += 360
        while (actualAngle > 360) actualAngle -= 360
        if (joint.jointSpeed < 0) angularRotation = 1
        else if (joint.jointSpeed > 0) angularRotation = -1
        if (actualAngle < 180) horizontalStickSide = -1
        else if (actualAngle > 180) horizontalStickSide = 1
        if (actualAngle < 90 && actualAngle > 270) verticalStickSide = 1
        else verticalStickSide = -1
        if (horizontalStickSide == -1) currentReward = (180f - actualAngle) / 180f
        else if (horizontalStickSide == 1) currentReward = (actualAngle - 180f) / 180f
    }

    fun moveOptimally(dt: Float) {

    }
    fun moveLeft(dt: Float) = base.setLinearVelocity(base.linearVelocity.x - 20*dt, 0f)
    fun moveRight(dt: Float) = base.setLinearVelocity(base.linearVelocity.x + 20*dt, 0f)

    override fun render(dt: Float) {
        update(dt)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        world.step(dt, 6, 2)

        game.shapeRender.begin(ShapeType.Filled)


        game.shapeRender.end()

        debugRender.render(world, game.phys2DCam.combined)

        game.batch.begin()

        game.font.draw(game.batch, "" + Gdx.graphics.framesPerSecond, game.width * 0.27f, game.height * 0.85f)
        game.font.draw(game.batch, "R = " + Math.round(currentReward*1000f)/1000f, game.width * 0.1f, game.height * 0.1f)
        game.font.draw(game.batch, "Angle = " + Math.round(actualAngle*10f)/10f, game.width * 0.8f, game.height * 0.1f)

        game.batch.end()

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