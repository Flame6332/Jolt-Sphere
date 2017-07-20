package org.joltsphere.scenes

import org.joltsphere.main.JoltSphereMain
import org.joltsphere.mechanics.WorldEntities
import org.joltsphere.mechanics.MapBodyBuilder
import org.joltsphere.mechanics.MountainClimbingPlayer
import org.joltsphere.mechanics.StreamBeamContactListener
import org.joltsphere.mechanics.StreamBeamTowedPlayer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.maps.MapProperties
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef

class Scene5(internal val game: JoltSphereMain) : Screen {

    internal var world: World
    internal var debugRender: Box2DDebugRenderer
    internal var contLis: StreamBeamContactListener
    internal var ent: WorldEntities

    internal var streamBeam: StreamBeamTowedPlayer
    internal var otherPlayer: MountainClimbingPlayer

    internal lateinit var map: TiledMap
    internal var mapProp: MapProperties
    internal var mapWidth: Int = 0
    internal var mapHeight: Int = 0

    internal var ppm = JoltSphereMain.ppm

    init {

        world = World(Vector2(0f, -9.8f), false) //ignore inactive objects false

        debugRender = Box2DDebugRenderer()

        ent = WorldEntities()
        contLis = StreamBeamContactListener()

        ent.createFlatPlatform(world)
        world = ent.world
        world.setContactListener(contLis)

        try {
            map = TmxMapLoader().load("testing/testmap.tmx")
        } catch (e: Exception) {
            println("Sumthin Broke")
        }

        mapProp = map.properties
        mapWidth = mapProp.get("width", Int::class.java) as Int * 320
        mapHeight = mapProp.get("height", Int::class.java) as Int * 320
        MapBodyBuilder.buildShapes(map, ppm, world, "terrain")

        streamBeam = StreamBeamTowedPlayer(world, 450, (mapHeight / 1.8f).toInt(), Color.RED)
        otherPlayer = MountainClimbingPlayer(world, 500f, (mapHeight / 1.8f).toInt().toFloat(), Color.BLUE)

        val dDef = DistanceJointDef()
        dDef.collideConnected = true
        dDef.bodyA = streamBeam.body
        dDef.length = 150 / ppm
        dDef.frequencyHz = 3f
        dDef.dampingRatio = 0.4f
        dDef.bodyB = otherPlayer.body
        dDef.localAnchorA.set(0f, 0f)
        dDef.localAnchorB.set(0f, 0f)
        world.createJoint(dDef)

    }

    private fun update(dt: Float) {
        streamBeam.input(Keys.P, Keys.P, Keys.P, Keys.P, Keys.LEFT, Keys.RIGHT, Keys.UP)
        streamBeam.reverseMovement(Keys.W, Keys.A, Keys.D)
        otherPlayer.input(Keys.W, Keys.S, Keys.A, Keys.D, Keys.SHIFT_LEFT, true)
    }


    override fun render(dt: Float) {
        update(dt)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        world.step(dt, 6, 2)

        streamBeam.update(dt, 1)
        otherPlayer.update(dt, 1)

        game.shapeRender.begin(ShapeType.Filled)

        streamBeam.shapeRender(game.shapeRender)
        otherPlayer.shapeRender(game.shapeRender)

        game.shapeRender.end()

        debugRender.render(world, game.phys2DCam.combined)

        game.batch.begin()

        game.font.draw(game.batch, "" + Gdx.graphics.framesPerSecond, game.width * 0.27f, game.height * 0.85f)

        game.batch.end()

        game.cam.position.x = otherPlayer.body.position.x * ppm
        game.cam.position.y = otherPlayer.body.position.y * ppm
        game.phys2DCam.position.x = otherPlayer.body.position.x
        game.phys2DCam.position.y = otherPlayer.body.position.y
        game.cam.zoom = 1f
        game.phys2DCam.zoom = 1f

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