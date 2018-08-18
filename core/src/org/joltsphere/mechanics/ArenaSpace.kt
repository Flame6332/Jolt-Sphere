package org.joltsphere.mechanics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array

class ArenaSpace(var width: Int, var height: Int) : ArenaPlayerEventListener {

    var players: Array<ArenaPlayer> = Array<ArenaPlayer>()

    init {}

    fun addPlayer(arenaPlayer: ArenaPlayer) {
        arenaPlayer.event = this // sets this extension of an event listener to
        players.add(arenaPlayer)
    }

    fun update(dt: Float, contacts: Array<Int>) {

        for (i in 0 until players.size) {
            players.get(i).update(contacts.get(i), dt, width, height)
        }

    }

    fun shapeRender(shapeRender: ShapeRenderer) {
        for (i in 0..players.size - 1) {
            players.get(i).shapeRender(shapeRender)
        }

        for (i in 0..players.first().paint.size - 1) { // all paint trails the same
            for (j in 0..players.size - 1) {
                players.get(j).renderPaint(shapeRender, i)
            }
        }

        shapeRender.rect(-10000 - width / 2f, height * 1.5f, 20000f, 200f, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE)
        shapeRender.rect(-10000 - width / 2f, height * -0.5f - 200, 20000f, 200f, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE)

    }

    fun input(player: Int, up: Int, down: Int, left: Int, right: Int, modifier: Int) {
        /*if (Gdx.input.isKeyPressed(modifier)) {
			if (Gdx.input.isKeyPressed(up)) playerMagnified(player);
		}*/
        if (Gdx.input.isKeyPressed(modifier))
            playerMagnified(player)
        else {
            if (Gdx.input.isKeyJustPressed(up))
                players.get(player).jump()
            else if (Gdx.input.isKeyPressed(down))
                players.get(player).smash() // added not smashing to notify when finger released
            else
                players.get(player).notSmashing() //place at top for smash density reset
            if (Gdx.input.isKeyPressed(left)) players.get(player).moveLeft(1f)
            if (Gdx.input.isKeyPressed(right)) players.get(player).moveRight(1f)
            if (Gdx.input.isKeyPressed(up)) players.get(player).jumpHold()
        }
        if (!Gdx.input.isKeyPressed(modifier)) players.get(player).notMagnifying()
    }

    override fun playerKnockedOut(knockedOutPlayer: ArenaPlayer) {
        for (player in players) {
            if (player != knockedOutPlayer) player.otherPlayerKnockedOut()
        }
    }

    fun playerMagnified(player: Int) {
        if (players.get(player).canMagnify()) {
            var magnifyingPlayer = Vector2()
            val otherPlayers = Array<Vector2>()
            for (i in 0..players.size - 1) {
                if (i == player) {
                    magnifyingPlayer = players.get(i).body.position
                } else {
                    otherPlayers.add(Vector2(players.get(i).body.position))
                }
            }
            players.get(player).magnify(otherPlayers.first())
            for (i in 0..players.size - 1) {
                if (i != player) {
                    players.get(i).otherPlayerMagnified(magnifyingPlayer)
                }
            }

        }
    }

}

interface ArenaPlayerEventListener {
    fun playerKnockedOut(arenaPlayer: ArenaPlayer) // called whenever a player knocked out
}