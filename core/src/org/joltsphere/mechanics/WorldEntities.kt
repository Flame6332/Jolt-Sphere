package org.joltsphere.mechanics

import org.joltsphere.main.JoltSphereMain
import org.joltsphere.misc.EllipseFixture

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType

class WorldEntities {

    internal var ppm = JoltSphereMain.ppm

    lateinit var world: World

    fun createPlatform1(world: World) {

        this.world = world

        val bdef = BodyDef()
        bdef.type = BodyType.StaticBody
        bdef.position.x = 0f
        bdef.position.y = 0f

        val chain = ChainShape()
        val v = arrayOfNulls<Vector2>(12)
        val x = JoltSphereMain.WIDTH / 2

        val xpnt1 = 800
        val ypnt1 = 300
        val xpnt2 = 760
        val ypnt2 = 250
        val xpnt3 = 690
        val ypnt3 = 190
        val xpnt4 = 600
        val ypnt4 = 140
        val xpnt5 = 490
        val ypnt5 = 115
        val xpnt6 = 410
        val ypnt6 = 100

        v[0] = Vector2((x - xpnt1) / ppm, ypnt1 / ppm)
        v[1] = Vector2((x - xpnt2) / ppm, ypnt2 / ppm)
        v[2] = Vector2((x - xpnt3) / ppm, ypnt3 / ppm)
        v[3] = Vector2((x - xpnt4) / ppm, ypnt4 / ppm)
        v[4] = Vector2((x - xpnt5) / ppm, ypnt5 / ppm)
        v[5] = Vector2((x - xpnt6) / ppm, ypnt6 / ppm)

        v[11] = Vector2((x + xpnt1) / ppm, ypnt1 / ppm)
        v[10] = Vector2((x + xpnt2) / ppm, ypnt2 / ppm)
        v[9] = Vector2((x + xpnt3) / ppm, ypnt3 / ppm)
        v[8] = Vector2((x + xpnt4) / ppm, ypnt4 / ppm)
        v[7] = Vector2((x + xpnt5) / ppm, ypnt5 / ppm)
        v[6] = Vector2((x + xpnt6) / ppm, ypnt6 / ppm)

        chain.createChain(v)
        val fdef = FixtureDef()
        fdef.shape = chain
        fdef.friction = 1f
        fdef.restitution = 0f

        val b = world.createBody(bdef)
        b.createFixture(fdef)
        b.userData = "ground"
        chain.dispose()

    }

    fun createFlatPlatform(world: World) {

        this.world = world

        val bdef = BodyDef()
        bdef.type = BodyType.StaticBody
        bdef.position.x = 0f
        bdef.position.y = 0f

        val w = JoltSphereMain.WIDTH
        val h = JoltSphereMain.HEIGHT
        val chain = ChainShape()
        val v = arrayOfNulls<Vector2>(5)

        v[0] = Vector2((0 + 1) / ppm, (0 + 1) / ppm)
        v[1] = Vector2((w - 1) / ppm, (0 + 1) / ppm)
        v[2] = Vector2((w - 1) / ppm, (h - 1) / ppm)
        v[3] = Vector2((0 + 1) / ppm, (h - 1) / ppm)
        v[4] = Vector2((0 + 1) / ppm, (1 + 1) / ppm)

        chain.createChain(v)
        val fdef = FixtureDef()
        fdef.shape = chain
        fdef.friction = 1f
        fdef.restitution = 0f

        world.createBody(bdef).createFixture(fdef).userData = "ground"

        chain.dispose()

    }

    fun createPlatform2(world: World) {

        this.world = world

        val bdef = BodyDef()
        bdef.type = BodyType.StaticBody
        bdef.position.x = 0f
        bdef.position.y = 0f

        val chain = ChainShape()
        val v = arrayOfNulls<Vector2>(12)
        val x = JoltSphereMain.WIDTH / 2

        val xpnt1 = 800
        val ypnt1 = 170
        val xpnt2 = 700
        val ypnt2 = 100
        val xpnt3 = 300
        val ypnt3 = 100
        val xpnt4 = 200
        val ypnt4 = 150
        val xpnt5 = 150
        val ypnt5 = 200
        val xpnt6 = 1
        val ypnt6 = 400

        v[0] = Vector2((x - xpnt1) / ppm, ypnt1 / ppm)
        v[1] = Vector2((x - xpnt2) / ppm, ypnt2 / ppm)
        v[2] = Vector2((x - xpnt3) / ppm, ypnt3 / ppm)
        v[3] = Vector2((x - xpnt4) / ppm, ypnt4 / ppm)
        v[4] = Vector2((x - xpnt5) / ppm, ypnt5 / ppm)
        v[5] = Vector2((x - xpnt6) / ppm, ypnt6 / ppm)

        v[11] = Vector2((x + xpnt1) / ppm, ypnt1 / ppm)
        v[10] = Vector2((x + xpnt2) / ppm, ypnt2 / ppm)
        v[9] = Vector2((x + xpnt3) / ppm, ypnt3 / ppm)
        v[8] = Vector2((x + xpnt4) / ppm, ypnt4 / ppm)
        v[7] = Vector2((x + xpnt5) / ppm, ypnt5 / ppm)
        v[6] = Vector2((x + xpnt6) / ppm, ypnt6 / ppm)

        chain.createChain(v)
        val fdef = FixtureDef()
        fdef.shape = chain
        fdef.friction = 1f

        world.createBody(bdef).createFixture(fdef).userData = "ground"

        chain.dispose()

    }

    fun createPlatform3(world: World) {

        this.world = world

        val bdef = BodyDef()
        bdef.type = BodyType.StaticBody
        bdef.position.x = 0f
        bdef.position.y = 0f

        val chain = ChainShape()
        val v = arrayOfNulls<Vector2>(12)
        val x = JoltSphereMain.WIDTH / 2

        val xpnt1 = 800
        val ypnt1 = -20
        val xpnt2 = 600
        val ypnt2 = 100
        val xpnt3 = 500
        val ypnt3 = 120
        val xpnt4 = 400
        val ypnt4 = 150
        val xpnt5 = 300
        val ypnt5 = 180
        val xpnt6 = 150
        val ypnt6 = 200

        v[0] = Vector2((x - xpnt1) / ppm, ypnt1 / ppm)
        v[1] = Vector2((x - xpnt2) / ppm, ypnt2 / ppm)
        v[2] = Vector2((x - xpnt3) / ppm, ypnt3 / ppm)
        v[3] = Vector2((x - xpnt4) / ppm, ypnt4 / ppm)
        v[4] = Vector2((x - xpnt5) / ppm, ypnt5 / ppm)
        v[5] = Vector2((x - xpnt6) / ppm, ypnt6 / ppm)

        v[11] = Vector2((x + xpnt1) / ppm, ypnt1 / ppm)
        v[10] = Vector2((x + xpnt2) / ppm, ypnt2 / ppm)
        v[9] = Vector2((x + xpnt3) / ppm, ypnt3 / ppm)
        v[8] = Vector2((x + xpnt4) / ppm, ypnt4 / ppm)
        v[7] = Vector2((x + xpnt5) / ppm, ypnt5 / ppm)
        v[6] = Vector2((x + xpnt6) / ppm, ypnt6 / ppm)

        chain.createChain(v)
        val fdef = FixtureDef()
        fdef.shape = chain
        fdef.friction = 1f

        val b = world.createBody(bdef)
                b.createFixture(fdef)
                b.userData = "ground"

        chain.dispose()

    }

    fun createPlatform4(world: World) {

        this.world = world

        val bdef = BodyDef()
        bdef.type = BodyType.StaticBody
        bdef.position.x = JoltSphereMain.WIDTH.toFloat() / 2f / ppm
        bdef.position.y = JoltSphereMain.HEIGHT * 0.0f / ppm

        val circle = CircleShape()
        circle.radius = 800 / ppm

        val fdef = FixtureDef()
        fdef.shape = circle
        fdef.friction = 1f

        world.createBody(bdef).createFixture(fdef).userData = "ground"
        fdef.density = 1f
        fdef.restitution = 0.5f
        bdef.position.y = 1200 / ppm
        bdef.type = BodyType.DynamicBody
        bdef.bullet = true
        EllipseFixture.createEllipseFixtures(world.createBody(bdef), fdef, 135 / ppm, 60 / ppm, "")
        bdef.position.y = 1700 / ppm
        EllipseFixture.createEllipseFixtures(world.createBody(bdef), fdef, 135 / ppm, 60 / ppm, "")
        bdef.position.y = 800 / ppm
        EllipseFixture.createEllipseFixtures(world.createBody(bdef), fdef, 135 / ppm, 60 / ppm, "")

        circle.dispose()


    }

}
