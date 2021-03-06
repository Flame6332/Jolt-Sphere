/* 10/29/2017 - Yousef Abdelgaber
*
*   This is my first attempt at making a Q-reinforcement learning algorithm. It's based off of
*   the classic reinforcement learning problem, "Grid World".
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
import org.joltsphere.misc.DeepQLearner
import org.joltsphere.misc.round
import org.joltsphere.misc.toF

class Scene9(internal val game: JoltSphereMain) : Screen {

    internal var ppm = JoltSphereMain.ppm

    //val tileColor1: Color = Color.DARK_GRAY
    //val tileColor2: Color = Color.BLACK
    val tileCountWidth = 4
    val tileCountHeight = 3

    val tileW = game.width/tileCountWidth.toF()
    val tileH = game.height/tileCountHeight.toF()

    val rowCount = (game.height/tileH).toInt()
    val columnCount = (game.width/tileW).toInt()
    //var isTileColor1 = true
    val lineWidth = 6f
    internal val player = Player(0,0)
    val tileMap = Array(columnCount, {Array(rowCount, {Tile()})})

    val aiController: DeepQLearner

    val discountFac = 0.99f
    val rewardPerMove = -0.04f
    val rewardPerTarget = 1f
    val rewardPerDeath = -1f

    var isExploring = true
    var isLive = false
    val minibatchSize = 12
    val learningRate = -1f

    val UP = 0; val RIGHT = 1; val DOWN = 2; val LEFT = 3;

    init {

        aiController = DeepQLearner(2, 4, intArrayOf(512,512),
                30, 0.0002f,
                -1f, 1f,
                1, 1, 0.2f, 4)

        aiController.name = "Bob from Grid World"
        aiController.isDebugEnabled = true

        createTiles1()

        for (i in 0 until columnCount) {
            tileMap[i][0].canMoveDown = false
            tileMap[i][rowCount-1].canMoveUp = false
        }
        for (i in 0 until rowCount) {
            tileMap[0][i].canMoveLeft = false
            tileMap[columnCount-1][i].canMoveRight = false
        }
        // loop sets booleans to grid surrounding objects
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

    fun createTiles1() {
        tileMap[1][1].isObstacle = true
        tileMap[3][1].isDeadly = true
        tileMap[3][2].isTarget = true
    }
    fun createTile2() {
        tileMap[8][8].isDeadly = true
        tileMap[0][3].isDeadly = true
        tileMap[5][6].isDeadly = true
        tileMap[1][1].isDeadly = true
        tileMap[2][3].isDeadly = true
        tileMap[2][4].isDeadly = true
        tileMap[13][2].isTarget = true
        tileMap[0][8].isTarget = true
        tileMap[7][8].isTarget = true
        tileMap[13][7].isTarget = true
        tileMap[14][6].isTarget = true
        tileMap[13][1].isObstacle = true
        tileMap[10][4].isObstacle = true
        tileMap[7][4].isObstacle = true
        tileMap[6][5].isObstacle = true
        tileMap[2][7].isObstacle = true
        tileMap[6][2].isObstacle = true
        tileMap[1][5].isObstacle = true
        tileMap[1][6].isObstacle = true
    }

    inner class Tile {
        var isObstacle = false
        var isDeadly = false
        var isTarget = false
        fun isTerminal(): Boolean = isDeadly || isTarget

        var canMoveUp = true
        var canMoveDown = true
        var canMoveLeft = true
        var canMoveRight = true
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
        fun getPreviousTile() = tileMap[previousTile.x.round()][previousTile.y.round()]

        fun attemptToTranslate(dir: Int) {
            when {
                dir == UP -> if (currentTile().canMoveUp) row++
                dir == DOWN -> if (currentTile().canMoveDown) row--
                dir == LEFT -> if (currentTile().canMoveLeft) col--
                dir == RIGHT -> if (currentTile().canMoveRight) col++
            }
        }

        /*// move, and if normmal tile,
        fun moveUp() {
            if (!currentTile().isSpecial()) { saveCurrentTile(); attemptToTranslate("up") }
            else terminate() }
        fun moveDown() {
            if (!currentTile().isSpecial()) { saveCurrentTile(); attemptToTranslate("down") }
            else terminate() }
        fun moveLeft() {
            if (!currentTile().isSpecial()) { saveCurrentTile(); attemptToTranslate("left") }
            else terminate() }
        fun moveRight() {
            if (!currentTile().isSpecial()) { saveCurrentTile(); attemptToTranslate("right") }
            else terminate() }
        fun moveIntelligently()  {

        }*/
        fun userMove(dir: Int) {
            aiController.actionSelectedExternally(dir)
            updateEnvironmentAndAgent()
        }
        fun updateEnvironmentAndAgent() {
            var reward = rewardPerMove
            if (currentTile().isTarget) reward = rewardPerTarget
            else if (currentTile().isDeadly) reward = rewardPerDeath
            if (currentTile().isTerminal()) {
                aiController.updateStateAndRewardThenSelectAction(floatArrayOf(row.toF(), col.toF()), reward, true, isExploring)
                terminate()
            }
            else
                attemptToTranslate(
                        aiController.updateStateAndRewardThenSelectAction(floatArrayOf(row.toF(), col.toF()), reward, false, isExploring))
            aiController.trainFromReplayMemory(minibatchSize, 1f)
        }
        fun terminate() {
            resetPlayer()
        }
        /*fun updateQValue(dir: String) {
            var prevQ = 0f
            when (dir) {
                "up" -> prevQ = getPreviousTile().upQ
                "down" -> prevQ = getPreviousTile().downQ
                "left" -> prevQ = getPreviousTile().leftQ
                "right" -> prevQ = getPreviousTile().rightQ
            }
            val reward = rewardPerMove
            val deltaQ: Float = learnRate * (reward + discountFac * currentTile().maxQ() - prevQ)
            when (dir) {
                "up" -> getPreviousTile().upQ += deltaQ
                "down" -> getPreviousTile().downQ += deltaQ
                "left" -> getPreviousTile().leftQ += deltaQ
                "right" -> getPreviousTile().rightQ += deltaQ
            }
        }*/
        /*fun terminate() {
            var reward = 0f
            if (currentTile().isDeadly) reward = rewardPerDeath
            else if (currentTile().isTarget) reward = rewardPerTarget
            val deltaQ: Float = learnRate * (reward + discountFac * currentTile().onlyQ) // termination Q function
            currentTile().onlyQ = deltaQ
            resetPlayer()
        }*/
    }

    internal fun update(dt: Float) {
        if (Gdx.input.isKeyJustPressed(Keys.UP)) player.userMove(UP)
        else if (Gdx.input.isKeyJustPressed(Keys.DOWN)) player.userMove(DOWN)
        else if (Gdx.input.isKeyJustPressed(Keys.LEFT)) player.userMove(LEFT)
        else if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) player.userMove(RIGHT)
        else if (isLive) player.updateEnvironmentAndAgent()
        else if (Gdx.input.isKeyJustPressed(Keys.Z)) player.updateEnvironmentAndAgent()

        if (Gdx.input.isKeyJustPressed(Keys.Q) && !isLive) isLive = true
        else if (Gdx.input.isKeyJustPressed(Keys.Q)) isLive = false

        if (Gdx.input.isKeyJustPressed(Keys.SPACE) && !isExploring) isExploring = true
        else if (Gdx.input.isKeyJustPressed(Keys.SPACE)) isExploring = false

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

        if (isExploring) game.shapeRender.color = Color.SKY
        else game.shapeRender.color = Color.GOLDENROD
        for (i in 0..columnCount) game.shapeRender.rectLine(tileW*i, 0f, tileW*i, tileH*rowCount, lineWidth) // Column lines
        for (i in 0..rowCount) game.shapeRender.rectLine(0f, tileH*i, tileW*columnCount, tileH*i, lineWidth) // Row line
        for (i in 0 until rowCount) game.shapeRender.rectLine(0f, tileH*i, (rowCount-i)*tileW, game.height, lineWidth) // Left-wall positive slope diagonal lines
        for (i in 1 until columnCount) game.shapeRender.rectLine(tileW*i, 0f, game.width, (columnCount-i)*tileH, lineWidth) // Bottom positive slope diagonal lines
        for (i in 1..rowCount) game.shapeRender.rectLine(0f, tileH*i, i*tileW, 0f, lineWidth) // Left-wall negative slope diagonal lines
        for (i in 1 until columnCount) game.shapeRender.rectLine(tileW*i, game.height, game.width, (rowCount-(columnCount-i))*tileH, lineWidth) // Top negative slope diagonal lines

        if (aiController.isExploring) game.shapeRender.color = Color.YELLOW
        else game.shapeRender.color = Color.TEAL
        game.shapeRender.circle(player.x(), player.y(), 90f)

        game.shapeRender.end()

        game.batch.begin()

        //game.font.draw(game.batch, "" + Gdx.graphics.framesPerSecond, game.width * 0.27f, game.height * 0.85f)
        val centerOffset = -40
        val vertOffset = 15
        game.font.data.setScale(0.5f)

        for (x in 0 until tileMap.size) {
            for (y in 0 until tileMap.first().size){
                when {
                    tileMap[x][y].isObstacle -> {
                        // something about walls
                    }
                    tileMap[x][y].isTarget -> game.font.draw(game.batch, "TARGET (NIRVANA)", tileW*x +20f, tileH*(y+1)-20f)
                    tileMap[x][y].isDeadly -> game.font.draw(game.batch, "" + aiController.explorationTimer, tileW*x +20f, tileH*(y+1)-20f)
                    else -> {
                        val output = aiController.neuralNetwork.feedforward(floatArrayOf(y.toF(),x.toF())) // TODO fix this huge waste in computing power as it won't scale properly
                        game.font.draw(game.batch, "U " + Math.round(output[UP] * 100f) / 100f, tileW * x + tileW / 2f + centerOffset, tileH * (y + 1) - 30) // up\
                        game.font.draw(game.batch, "R " + Math.round(output[RIGHT] * 100f) / 100f, tileW * x + 0.73f * tileW, tileH * y + tileH / 2f - vertOffset)
                        game.font.draw(game.batch, "D " + Math.round(output[DOWN] * 100f) / 100f, tileW * x + tileW / 2f + centerOffset, tileH * y + 65f)
                        game.font.draw(game.batch, "L " + Math.round(output[LEFT] * 100f) / 100f, tileW * x + 23, tileH * y + tileH / 2f + vertOffset)
                    }
                }
            }
        }

        game.font.data.setScale(1f)

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