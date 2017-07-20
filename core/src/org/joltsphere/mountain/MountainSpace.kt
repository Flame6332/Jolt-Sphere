package org.joltsphere.mountain

import org.joltsphere.main.JoltSphereMain
import org.joltsphere.mechanics.MountainClimbingPlayer
import org.joltsphere.mechanics.StreamBeamPlayer
import org.joltsphere.misc.EllipseFixture
import org.joltsphere.misc.Misc
import org.joltsphere.misc.ObjectData

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType

class MountainSpace(private val world: World) {
    private val streamBeam: StreamBeamPlayer
    private val mountainClimber: MountainClimbingPlayer

    private val sideWalls: Array<SideWall>
    private val risingPlatform: RisingPlatform
    private val platforms: Array<Platform>
    //private Array<Body> fixturesToBeDestroyed;
    private var highestPlatform = 0f
    private var tallestSideWall = 0f
    private val platformPopulationDensity = 150f
    private var risingPlatformHeight: Float = 0.toFloat()
    private var sideWallCount = 1f

    var points = 0
    private var streamBeamGroundContacts = 0
    private var mountainClimberGroundContacts = 0

    private val ppm = JoltSphereMain.ppm
    private val width = JoltSphereMain.WIDTH
    private val height = JoltSphereMain.HEIGHT
    private val mountainWidth = width.toFloat()
    private val borderSize = (width - mountainWidth) / 2f
    private val risingSpeed = 0.7f

    init {
        world.setContactListener(ContLis())
        streamBeam = StreamBeamPlayer(world, width / 2f, height.toFloat(), Color.RED)
        mountainClimber = MountainClimbingPlayer(world, width / 2f, height.toFloat(), Color.BLUE)

        risingPlatform = RisingPlatform()
        sideWalls = Array<SideWall>()
        platforms = Array<Platform>()
        //fixturesToBeDestroyed = new Array<Body>();

    }

    fun shapeRender(shapeRender: ShapeRenderer) {
        streamBeam.shapeRender(shapeRender)
        mountainClimber.shapeRender(shapeRender)

        //shapeRender.setColor((float) Math.sin(risingPlatformHeight), (float) Math.cos(risingPlatformHeight), (float) Math.tan(risingPlatformHeight), 1);
        //shapeRender.rect(0, 0, width, risingPlatformHeight*ppm +20);

        shapeRender.setColor(193 / 255f, 138 / 255f, 0f, 0.24f)
        //shapeRender.setColor(Color.RAINBOW);
        shapeRender.rect(0f, 0f, width.toFloat(), risingPlatformHeight * ppm + 20)
        /*shapeRender.setColor(193/255f, 138/255f, 0, 0.24f);
		shapeRender.rect(0, 0, width, risingPlatformHeight*ppm +20);
		shapeRender.setColor(Color.BROWN);
		for (int i = 1; i <= 7; i++) {
			shapeRender.ellipse(Misc.random(0, 100) +i*(width / 8)  + 50, risingPlatformHeight*ppm, Misc.random(100, 200), Misc.random(30, 90));
		}*/
    }

    fun update(dt: Float, viewHalfWidth: Float, viewHalfHeight: Float) {
        var viewHalfHeight = viewHalfHeight
        risingPlatformHeight = risingPlatform.height
        viewHalfHeight = viewHalfHeight * 2f * getZoom(viewHalfHeight * 2)

        streamBeam.input(Keys.UP, Keys.DOWN, Keys.LEFT, Keys.RIGHT, Keys.K, Keys.SEMICOLON, Keys.O)
        mountainClimber.input(Keys.W, Keys.S, Keys.A, Keys.D, Keys.SHIFT_LEFT, false)
        streamBeam.update(dt, streamBeamGroundContacts)
        mountainClimber.update(dt, mountainClimberGroundContacts)

        while (cameraPostion.y + viewHalfHeight > highestPlatform) {
            highestPlatform += platformPopulationDensity
            platforms.add(Platform())
        }
        for (platform in platforms) {
            if (platform.isDead) platforms.removeValue(platform, true)
        }
        if (cameraPostion.y + viewHalfHeight > tallestSideWall) {
            sideWalls.add(SideWall())
            sideWallCount++
            for (sideWall in sideWalls) {
                if (sideWall.isDead) sideWalls.removeValue(sideWall, true)
            }
        }

    }

    private inner class ContLis : ContactListener {
        private var bA: Body? = null
        private var bB: Body? = null
        override fun beginContact(contact: Contact) {
            bA = contact.fixtureA.body
            bB = contact.fixtureB.body
            if (isContacting("mountainClimber", "ground")) mountainClimberGroundContacts++
            if (isContacting("streamBeam", "ground")) streamBeamGroundContacts++
            if (isContacting("deathPlatform", "streamBeam"))
                if (isContacting("deathPlatform", "mountainClimber"))
                    if (isContacting("fire", "mountainClimber")) {
                        if ((bA!!.userData as ObjectData).string === "fire")
                            streamBeam.firedBallsToBeRemoved.add((bA!!.userData as ObjectData).count)
                        else
                            streamBeam.firedBallsToBeRemoved.add((bB!!.userData as ObjectData).count)
                        points++
                    }
        }

        override fun endContact(contact: Contact) {
            bA = contact.fixtureA.body
            bB = contact.fixtureB.body
            if (isContacting("mountainClimber", "ground")) mountainClimberGroundContacts--
            if (isContacting("streamBeam", "ground")) streamBeamGroundContacts--
        }

        override fun preSolve(contact: Contact, oldManifold: Manifold) {}
        override fun postSolve(contact: Contact, impulse: ContactImpulse) {}
        private fun isContacting(string1: String, string2: String): Boolean {
            val dat1: String
            val dat2: String
            if (bA!!.userData is ObjectData) {
                dat1 = (bA!!.userData as ObjectData).string
            } else
                dat1 = bA!!.userData as String
            if (bB!!.userData is ObjectData) {
                dat2 = (bB!!.userData as ObjectData).string
            } else
                dat2 = bB!!.userData as String

            if (dat1 === string1 && dat2 === string2)
                return true
            else if (dat1 === string2 && dat2 === string1)
                return true
            else
                return false
        }
    }

    /*for (Body f : fixturesToBeDestroyed) {
        System.out.println(f.getUserData());
        fixturesToBeDestroyed.removeValue(f, true);
        System.out.println("removed value");
        if (f.getUserData() != null) world.destroyBody(f);
        System.out.println("fixture destroyed");
    }*/

    private inner class RisingPlatform {
        internal var body: Body

        init {
            val bdef = BodyDef()
            bdef.type = BodyType.KinematicBody
            bdef.position.x = width.toFloat() / 2f / ppm
            bdef.position.y = -40 / ppm
            val polygon = PolygonShape()
            polygon.setAsBox(mountainWidth / 2f / ppm, 20 / ppm)
            val fdef = FixtureDef()
            fdef.shape = polygon
            fdef.friction = 3f
            fdef.restitution = 0f
            body = world.createBody(bdef)
            body.createFixture(fdef).userData = "deathPlatform"
            body.setLinearVelocity(0f, risingSpeed)
            polygon.dispose()
        }

        val height: Float
            get() = body.position.y
    }

    private inner class Platform {

        private val body: Body

        init {
            val bdef = BodyDef()
            bdef.type = BodyType.StaticBody
            bdef.position.set(Misc.random() * mountainWidth / ppm, highestPlatform / ppm)
            body = world.createBody(bdef)
            body.userData = "ground"
            val size = 400f
            EllipseFixture.createEllipseFixtures(body, 1f, 0f, 1f, Misc.random(0.1f, 1f) * size / ppm, Misc.random(0.1f, 1f) * size / ppm, null)
        }

        val isDead: Boolean
            get() {
                if (body.position.y < risingPlatformHeight) {
                    world.destroyBody(body)
                    return true
                } else
                    return false
            }
    }

    private inner class SideWall {

        private val leftBody: Body
        private val rightBody: Body
        var isDead = false

        init {
            val bdef = BodyDef()
            bdef.type = BodyType.StaticBody
            val polygon = PolygonShape()
            polygon.setAsBox(20 / ppm, height / ppm)
            val fdef = FixtureDef()
            fdef.shape = polygon
            fdef.friction = 10f
            fdef.restitution = 0f
            bdef.position.set(borderSize, height * sideWallCount / ppm)
            leftBody = world.createBody(bdef)
            bdef.position.x = (borderSize + mountainWidth) / ppm
            rightBody = world.createBody(bdef)
            leftBody.createFixture(fdef).userData = ""
            rightBody.createFixture(fdef).userData = ""
            polygon.dispose()
            tallestSideWall = height * sideWallCount + height
        }

    }

    fun getZoom(viewPortHeight: Float): Float {
        val pad = 1.2f // amount of padding so it isnt on the border all the time
        if (viewPortHeight > Math.abs(streamBeam.body.position.y - mountainClimber.body.position.y) * pad * ppm)
            return 1f
        else
            return Math.abs(streamBeam.body.position.y - mountainClimber.body.position.y) * pad * ppm / viewPortHeight
    }

    val debugCameraPostion: Vector2
        get() = Vector2(width.toFloat() / 2f / ppm, (streamBeam.body.position.y + mountainClimber.body.position.y) / 2f)
    val cameraPostion: Vector2
        get() = Vector2(debugCameraPostion.x * ppm, debugCameraPostion.y * ppm)
}
 