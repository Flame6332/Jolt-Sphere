package org.joltsphere.scenes

import org.joltsphere.main.JoltSphereMain
import org.joltsphere.mechanics.WorldEntities
import org.joltsphere.mechanics.StreamBeamContactListener
import org.joltsphere.mechanics.StreamBeamPlayer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World

class Scene2(internal val game: JoltSphereMain) : Screen {

    internal var world: World
    internal var debugRender: Box2DDebugRenderer
    internal var contLis: StreamBeamContactListener
    internal var ent: WorldEntities

    internal var streamBeam: StreamBeamPlayer
    internal var otherPlayer: StreamBeamPlayer

    internal var ppm = JoltSphereMain.ppm

    init {

        world = World(Vector2(0f, -9.8f), false) //ignore inactive objects false

        debugRender = Box2DDebugRenderer()

        ent = WorldEntities()
        contLis = StreamBeamContactListener()

        ent.createFlatPlatform(world)
        ent.createPlatform4(world)
        world.setContactListener(contLis)

        streamBeam = StreamBeamPlayer(world, 200f, 200f, Color.RED)
        otherPlayer = StreamBeamPlayer(world, 1600f, 200f, Color.BLUE)

    }

    private fun update(dt: Float) {
        streamBeam.input(Keys.UP, Keys.DOWN, Keys.LEFT, Keys.RIGHT, Keys.K, Keys.SEMICOLON, Keys.O)
        otherPlayer.input(Keys.W, Keys.S, Keys.A, Keys.D, Keys.F, Keys.H, Keys.T)
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