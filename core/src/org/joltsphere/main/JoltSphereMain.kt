package org.joltsphere.main

import com.badlogic.gdx.Game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import org.joltsphere.scenes.*

open class JoltSphereMain : Game() {

    companion object {
        var displayScale :Int = 200
        var WIDTH = 16 * displayScale
        var HEIGHT = 9 * displayScale
        val FPS = 60
        var title = "Jolt Sphere"
        val ppm = 200f //pixels per meter
    }

    var width :Float = 16f * displayScale
    var height :Float = 9f * displayScale
    lateinit var font: BitmapFont //testing font

    private lateinit var fontTex: Texture //texture for font
    var cam: OrthographicCamera = OrthographicCamera()
    var uiCam: OrthographicCamera = OrthographicCamera()
    var phys2DCam: OrthographicCamera = OrthographicCamera()
    lateinit var view: Viewport
    lateinit var phys2Dview: Viewport

    lateinit var uiView: Viewport
    lateinit var batch: SpriteBatch

    lateinit var shapeRender: ShapeRenderer

    override fun create() {

        fontTex = Texture(Gdx.files.internal("testing/font.png"), true) //mipmaps=true
        fontTex.setFilter(TextureFilter.Linear, TextureFilter.Linear)
        font = BitmapFont(Gdx.files.internal("testing/font.fnt"), TextureRegion(fontTex), false) //flipped=font

        cam.setToOrtho(false, width, height)
        uiCam.setToOrtho(false, width, height)
        phys2DCam.setToOrtho(false, width / ppm, height / ppm) //physics world by meters
        view = ExtendViewport(width, height, cam)
        uiView = ExtendViewport(width, height, uiCam)
        phys2Dview = ExtendViewport(width / ppm, height / ppm, phys2DCam)

        shapeRender = ShapeRenderer()

        this.setScreen(Scene1(this))
        batch = SpriteBatch()

        Gdx.graphics.setVSync(true)
        setFPSLimit(60)
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode())
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if (Gdx.input.isKeyJustPressed(Keys.TAB)) switchScene()

        super.render() //renders in screens

        cam.position.set(width / 2, height / 2, 0f)
        cam.zoom = 1f
        phys2DCam.position.set(width / 2f / ppm, height / 2f / ppm, 0f)
        phys2DCam.zoom = 1f

        Gdx.graphics.setTitle(title + " : " + subtitle + "     FPS: " + Gdx.graphics.getFramesPerSecond())

        if (Gdx.input.isKeyJustPressed(Keys.ENTER) && !Gdx.graphics.isFullscreen())
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode())
        else if (Gdx.input.isKeyJustPressed(Keys.ENTER)) Gdx.graphics.setWindowedMode(width.toInt(), height.toInt())

        batch.setProjectionMatrix(uiCam.combined)
        shapeRender.setProjectionMatrix(cam.combined)
    }

    internal var subtitle = "Basic"

    internal var currentScene = 1

    fun switchScene() {
        currentScene++
        when (currentScene) {
            1 -> {
                this.setScreen(Scene1(this))
                subtitle = "Arena"
            }
            2 -> {
                this.setScreen(Scene2(this))
                subtitle = "StreamBeam"
            }
            3 -> {
                this.setScreen(Scene3(this))
                subtitle = "Mountain Climber"
            }
            4 -> {
                this.setScreen(Scene4(this))
                subtitle = "Tower Defense"
            }
            5 -> {
                this.setScreen(Scene5(this))
                subtitle = "Ninja Chase"
            }
            6 -> {
                this.setScreen(Scene6(this))
                subtitle = "Reinforcement Learning Balance Stick"
            }
            else -> {
                this.setScreen(Scene1(this))
                currentScene = 1
                subtitle = "Arena"
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        view.update(width, height)
        uiView.update(width, height)
        phys2Dview.update(width, height)
    }

    override fun dispose() {

    }

    inner class input

    open protected fun setFPSLimit(value: Int) {} // overidden in desktop launcher

}