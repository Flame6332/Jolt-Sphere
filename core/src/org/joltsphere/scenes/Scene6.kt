/* 10/29/2017 - Yousef Abdelgaber
*
*   This is my first attempt at making a Q-reinforcement learning algorithim. It's based off of the classic reinforcement learning problem, "Grid World".
*
 */

package org.joltsphere.scenes

import org.joltsphere.main.JoltSphereMain

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2

class Scene6(internal val game: JoltSphereMain) : Screen {

    internal var ppm = JoltSphereMain.ppm

    //val tileColor1: Color = Color.DARK_GRAY
    //val tileColor2: Color = Color.BLACK
    val tileW = game.width/4f
    val tileH = game.height/3f
    val rowCount = (game.height/tileH).toInt()
    val columnCount = (game.width/tileW).toInt()
    //var isTileColor1 = true
    val lineWidth = 6f
    internal val player = Player(0,0)
    val tileMap = Array(columnCount, {Array(rowCount, {Tile()})})

    val learnRate = 0.5f
    val discountFac = 1f
    val rewardPerMove = -0.04f
    val rewardPerTarget = 1f
    val rewardPerDeath = -1f

    init {
        tileMap[1][1].isObstacle = true
        tileMap[3][1].isDeadly = true
        //tileMap[5][6].isDeadly = true
        tileMap[3][2].isTarget = true
        //tileMap[13][2].isTarget = true

        for (i in 0 until columnCount) {
            tileMap[i][0].canMoveDown = false
            tileMap[i][rowCount-1].canMoveUp = false
        }
        for (i in 0 until rowCount) {
            tileMap[0][i].canMoveLeft = false
            tileMap[columnCount-1][i].canMoveRight = false
        }
        for (x in 0 until tileMap.size) {
            for (y in 0 until tileMap.first().size) {
                if (tileMap[x][y].isObstacle) {
                    if (x != 0) tileMap[x-1][y].canMoveRight = false
                    if (x != columnCount-1) tileMap[x+1][y].canMoveLeft = false
                    if (y != 0) tileMap[x][y-1].canMoveUp = false
                    if (y != rowCount-1) tileMap[x][y+1].canMoveDown = false
                }
            }
        }
    }

    inner class Tile() {
        var isObstacle = false
        var isDeadly = false
        var isTarget = false
        fun isSpecial(): Boolean = isDeadly || isTarget

        var canMoveUp = true
        var canMoveDown = true
        var canMoveLeft = true
        var canMoveRight = true

        var upQ = 0f
        var downQ = 0f
        var leftQ = 0f
        var rightQ = 0f
        var onlyQ = 0f

        fun maxQ(): Float {
            if (!isSpecial()) return floatArrayOf(upQ,downQ,leftQ,rightQ).max()!!
            else return onlyQ
        }
        fun optimalDirection(): Vector2 {
            if (upQ == maxQ()) return Vector2(0f, 1f)
            else if (rightQ == maxQ()) return Vector2(1f, 0f)
            else if (leftQ == maxQ()) return Vector2(-1f, 0f)
            else return Vector2(0f, -1f) // go down
        }
    }

    inner class Player(val initialCol: Int, val initialRow: Int) {
        var row = initialRow
        var col = initialCol
        fun resetPlayer() {
            row = initialRow
            col = initialCol
        }

        fun x(): Float = (col+1)*tileW - tileW/2f
        fun y(): Float = (row+1)*tileH - tileH/2f

        fun currentTile() = tileMap[col][row]
        var previousTile = Vector2()
        fun saveCurrentTile() { previousTile.set(col.toFloat(), row.toFloat()) }
        fun getPreviousTile() = tileMap[previousTile.x.toInt()][previousTile.y.toInt()]

        fun moveUp() {
            if (!currentTile().isSpecial()) { saveCurrentTile(); if (currentTile().canMoveUp) row++; updateQValue("up") }
            else terminate() }
        fun moveDown() {
            if (!currentTile().isSpecial()) { saveCurrentTile(); if (currentTile().canMoveDown) row--; updateQValue("down") }
            else terminate() }
        fun moveLeft() {
            if (!currentTile().isSpecial()) { saveCurrentTile(); if (currentTile().canMoveLeft) col--; updateQValue("left") }
            else terminate() }
        fun moveRight() {
            if (!currentTile().isSpecial()) { saveCurrentTile(); if (currentTile().canMoveRight) col++; updateQValue("right") }
            else terminate() }
        fun moveOptimally()  {
            val optimalDir = currentTile().optimalDirection()
            if (optimalDir.y == 1f) moveUp()
            else if (optimalDir.y == -1f) moveDown()
            else if (optimalDir.x == 1f) moveRight()
            else if (optimalDir.x == -1f) moveLeft()
        }
        fun updateQValue(dir: String) {
            var prevQ = 0f
            if (dir == "up") prevQ = getPreviousTile().upQ
            else if (dir == "down") prevQ = getPreviousTile().downQ
            else if (dir == "left") prevQ = getPreviousTile().leftQ
            else if (dir == "right") prevQ = getPreviousTile().rightQ
            val reward = rewardPerMove
            val deltaQ: Float = learnRate * (reward + discountFac * currentTile().maxQ() - prevQ)
            if (dir == "up") getPreviousTile().upQ += deltaQ
            else if (dir == "down") getPreviousTile().downQ += deltaQ
            else if (dir == "left") getPreviousTile().leftQ += deltaQ
            else if (dir == "right") getPreviousTile().rightQ += deltaQ
        }
        fun terminate() {
            var reward = 0f
            if (currentTile().isDeadly) reward = -1f
            else if (currentTile().isTarget) reward = 1f
            val deltaQ: Float = learnRate * (reward + discountFac * currentTile().onlyQ) // termination Q function
            currentTile().onlyQ = deltaQ
            resetPlayer()
        }
    }

    internal fun update(dt: Float) {

        if (Gdx.input.isKeyJustPressed(Keys.UP)) player.moveUp()
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) player.moveDown()
        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) player.moveLeft()
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) player.moveRight()
        if (Gdx.input.isKeyJustPressed(Keys.Z)) player.moveOptimally()
        else if (Gdx.input.isKeyPressed(Keys.Q)) player.moveOptimally()

    }

    /*internal fun flipTileColors() {
        if (isTileColor1) {
            game.shapeRender.color = tileColor2
            isTileColor1 = false
        } else {
            game.shapeRender.color = tileColor1
            isTileColor1 = true
        }
    }*/

    override fun render(dt: Float) {
        update(dt)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        game.shapeRender.begin(ShapeType.Filled)

        /*game.shapeRender.color = tileColor1
        for (i in 1..rowCount) {
            flipTileColors()
            for (j in 1..columnCount) {
                flipTileColors()
                game.shapeRender.rect(tileW*(j-1), tileH*(i-1), tileW, tileH)
            }
        }
        flipTileColors()*/

        for (x in 0 until tileMap.size) {
            for (y in 0 until tileMap.first().size){
                if (tileMap[x][y].isObstacle) {
                    game.shapeRender.color = Color.GRAY
                    game.shapeRender.rect(x*tileW, y*tileH, tileW, tileH)
                }
                if (tileMap[x][y].isTarget) {
                    game.shapeRender.color = Color.GREEN
                    game.shapeRender.rect(x*tileW, y*tileH, tileW, tileH)
                }
                if (tileMap[x][y].isDeadly) {
                    game.shapeRender.color = Color.RED
                    game.shapeRender.rect(x*tileW, y*tileH, tileW, tileH)
                }
            }
        }

        game.shapeRender.color = Color.GOLDENROD
        for (i in 0..columnCount) game.shapeRender.rectLine(tileW*i, 0f, tileW*i, tileH*rowCount, lineWidth) // Column lines
        for (i in 0..rowCount) game.shapeRender.rectLine(0f, tileH*i, tileW*columnCount, tileH*i, lineWidth) // Row line
        for (i in 0 until rowCount) game.shapeRender.rectLine(0f, tileH*i, (rowCount-i)*tileW, game.height, lineWidth) // Left-wall positive slope diagonal lines
        for (i in 1 until columnCount) game.shapeRender.rectLine(tileW*i, 0f, game.width, (columnCount-i)*tileH, lineWidth) // Bottom positive slope diagonal lines
        for (i in 1..rowCount) game.shapeRender.rectLine(0f, tileH*i, i*tileW, 0f, lineWidth) // Left-wall negative slope diagonal lines
        for (i in 1 until columnCount) game.shapeRender.rectLine(tileW*i, game.height, game.width, (rowCount-(columnCount-i))*tileH, lineWidth) // Top negative slope diagonal lines

        game.shapeRender.color = Color.TEAL
        game.shapeRender.circle(player.x(), player.y(), 90f)

        game.shapeRender.end()

        game.batch.begin()

        //game.font.draw(game.batch, "" + Gdx.graphics.framesPerSecond, game.width * 0.27f, game.height * 0.85f)
        val centerOffset = -40
        val vertOffset = 15

        for (x in 0 until tileMap.size) {
            for (y in 0 until tileMap.first().size){
                if (tileMap[x][y].isObstacle) {
                    // TODO add necessary joke about about Mexicans
                }
                else if (tileMap[x][y].isTarget) {
                    game.font.draw(game.batch, "" + Math.round(tileMap[x][y].onlyQ*100f)/100f, tileW*x +20f, tileH*(y+1)-20f)
                }
                else if (tileMap[x][y].isDeadly) {
                    game.font.draw(game.batch, "" + Math.round(tileMap[x][y].onlyQ*100f)/100f, tileW*x +20f, tileH*(y+1)-20f)
                }
                else {
                    game.font.draw(game.batch, "" + Math.round(tileMap[x][y].upQ * 100f) / 100f, tileW * x + tileW / 2f + centerOffset, tileH * (y + 1) - 30)
                    game.font.draw(game.batch, "" + Math.round(tileMap[x][y].downQ * 100f) / 100f, tileW * x + tileW / 2f + centerOffset, tileH * y + 65f)
                    game.font.draw(game.batch, "" + Math.round(tileMap[x][y].leftQ * 100f) / 100f, tileW * x + 23, tileH * y + tileH / 2f + vertOffset)
                    game.font.draw(game.batch, "" + Math.round(tileMap[x][y].rightQ * 100f) / 100f, tileW * x + 0.73f * tileW, tileH * y + tileH / 2f + vertOffset)
                }
            }
        }

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