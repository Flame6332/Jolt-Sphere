package org.joltsphere.scenes

import org.joltsphere.main.JoltSphereMain
import org.joltsphere.mountain.MountainSpace
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World

class Scene3(internal val game: JoltSphereMain) : Screen {

    internal var world: World
    internal var debugRender: Box2DDebugRenderer

    internal var mountainSpace: MountainSpace

    internal var ppm = JoltSphereMain.ppm

    init {

        world = World(Vector2(0f, -9.8f), false) //ignore inactive objects false

        debugRender = Box2DDebugRenderer()

        mountainSpace = MountainSpace(world)

    }

    private fun update(dt: Float) {
        mountainSpace.update(dt, game.cam.viewportWidth / 2f, game.cam.viewportHeight / 2f)
        world.step(dt, 6, 2)
    }


    override fun render(dt: Float) {
        update(dt)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        game.shapeRender.begin(ShapeType.Filled)

        mountainSpace.shapeRender(game.shapeRender)

        game.shapeRender.end()

        debugRender.render(world, game.phys2DCam.combined)

        game.batch.begin()

        //game.font.draw(game.batch, mountainSpace.points + "boombooms      FPS: " + Gdx.graphics.getFramesPerSecond(), game.width*0.27f, game.height * 0.85f);

        game.batch.end()

        val zoom = mountainSpace.getZoom(game.cam.viewportHeight)
        game.cam.zoom = zoom
        game.phys2DCam.zoom = zoom
        game.cam.position.set(mountainSpace.cameraPostion, 0f)
        game.phys2DCam.position.set(mountainSpace.debugCameraPostion, 0f)

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