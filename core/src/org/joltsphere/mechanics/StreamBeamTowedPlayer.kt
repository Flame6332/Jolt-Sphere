package org.joltsphere.mechanics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.physics.box2d.World

class StreamBeamTowedPlayer// overrides the density variable in super class

(world: World, x: Float, y: Float, color: Color) : StreamBeamPlayer(world, x, y, color) {

    override fun density(): Float {
        return 0.3f
    }

    init {

        body.linearDamping = 100000f
    }

    fun reverseMovement(up: Int, left: Int, right: Int) {
        val x = 15
        if (Gdx.input.isKeyPressed(up)) body.applyForceToCenter(0f, -10f, true)
        if (Gdx.input.isKeyPressed(left)) body.applyForceToCenter(x.toFloat(), 0f, true)
        if (Gdx.input.isKeyPressed(right)) body.applyForceToCenter((-x).toFloat(), 0f, true)
    }
}
