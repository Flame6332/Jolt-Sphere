package org.joltsphere.scenes

import org.joltsphere.main.JoltSphereMain
import org.joltsphere.mechanics.WorldEntities
import org.joltsphere.mechanics.ArenaPlayer
import org.joltsphere.mechanics.ArenaSpace
import org.joltsphere.mechanics.ArenaContactListener

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World

/* TODO
	 * - Move contact listener and world stuff into the arenaspace for easier world stuff
	 * - Remove unused variables
	 * - Change up player addition to accomodate for previous change
	 */
class Scene1 (internal val game: JoltSphereMain) : Screen {

    internal var world: World  = World(Vector2(0f, -9.8f), false) //ignore inactive objects false

    internal var contLis: ArenaContactListener
    internal var debugRender: Box2DDebugRenderer = Box2DDebugRenderer()
    internal var ent: WorldEntities = WorldEntities()

    internal var arena: ArenaSpace

    internal var timer = 0f
    internal var isTimerEnabled = true
    internal var ppm = JoltSphereMain.ppm

    init {
        arena = ArenaSpace(game.width.toInt(), game.height.toInt())
        ent.createPlatform1(world)

        val x = 300
        arena.addPlayer(ArenaPlayer((game.width / 2 + x).toInt(), 300, world, 1, Color.FIREBRICK))
        arena.addPlayer(ArenaPlayer((game.width / 2 - x).toInt(), 300, world, 2, Color.BLUE))

        contLis = ArenaContactListener(arena.players.size)
        world.setContactListener(contLis)
    }

    internal fun update(dt: Float) {

        arena.input(0, Keys.UP, Keys.DOWN, Keys.LEFT, Keys.RIGHT, Keys.CONTROL_RIGHT)
        arena.input(1, Keys.W, Keys.S, Keys.A, Keys.D, Keys.SHIFT_LEFT)
        arena.update(dt, contLis.playerContacts)

        if (Gdx.input.isKeyJustPressed(Keys.P)) {
            isTimerEnabled = true
            arena.players.get(0).knockouts = 0
            arena.players.get(1).knockouts = 0
        }

        if (isTimerEnabled) {
            if (timer < 0) {
                isTimerEnabled = false
                timer = (5 * 60).toFloat()
            }
            timer -= dt
        }

        if (contLis.pvpContact > 0) {
            arena.players.get(0).contactingOtherPlayer(arena.players.get(1))
            arena.players.get(1).contactingOtherPlayer(arena.players.get(0))
        } else {
            arena.players.get(0).notContactingOtherPlayer(arena.players.get(1))
            arena.players.get(1).notContactingOtherPlayer(arena.players.get(0))
        }

        /*if (Controllers.getControllers().get(0).getButton(XBox360.BUTTON_A)) {
			if (game.getControllers(0).getButton(game.BUTTON_A)) {
				Controllers.getControllers().get(0).
				arena.players.get(0).body.applyForceToCenter(1000, 0, true);
			}
		}*/

    }


    override fun render(dt: Float) {
        update(dt)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        world.step(dt, 6, 2)

        game.shapeRender.begin(ShapeType.Filled)

        arena.shapeRender(game.shapeRender)

        game.shapeRender.end()

        debugRender.render(world, game.phys2DCam.combined)

        game.batch.begin()

        game.font.draw(game.batch, "" + arena.players.get(0).knockouts, game.width * 0.27f, game.height * 0.085f)
        game.font.draw(game.batch, "" + arena.players.get(1).knockouts, game.width * 0.72f, game.height * 0.085f)

        if (arena.players.get(0).canSmash) game.font.draw(game.batch, "Jolt! < ^ >", game.width * 0.85f, game.height * 0.1f)
        if (arena.players.get(1).canSmash) game.font.draw(game.batch, "Jolt! WASD", game.width * 0.05f, game.height * 0.1f)

        if (isTimerEnabled) game.font.draw(game.batch, (timer / 60).toInt().toString() + " : " + Math.round((timer / 60 - (timer / 60).toInt()) * 60), game.width * 0.46f, game.height * 0.92f)

        //game.font.draw(game.batch, Math.round((arena.players.get(0).fdefBall.density / 5 * 100) * 10f)/10f + "%", game.width * 0.85f, game.height * 0.6f);
        //game.font.draw(game.batch, Math.round((arena.players.get(0).energyTimer * 100) * 10f)/10f + "%", game.width * 0.85f, game.height * 0.6f);

        game.font.draw(game.batch, (Math.round(arena.players.get(0).currentRecievingSmashRestitution * 10f) / 10f).toString() + "x", game.width * 0.85f, game.height * 0.6f)
        game.font.draw(game.batch, (Math.round(arena.players.get(1).currentRecievingSmashRestitution * 10f) / 10f).toString() + "x", game.width * 0.06f, game.height * 0.6f)

        game.batch.end()


        /*objectPositions = new Array<Vector2>();
		objectPositions.add(arena.players.get(1).getPosition());
		objectPositions.add(arena.players.get(1).getPosition());
		objectPositions.add(worldCenter);
		Vector3 pos = findZoom(objectPositions);

		//game.cam.position.set(pos.x, pos.y, 0);
		//game.cam.zoom = pos.z;
		//game.phys2DCam.position.set(pos.x/ppm, pos.y/ppm, 0);
		//game.phys2DCam.zoom = pos.z;
		*/
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


/*private Vector3 findZoom(Array<Vector2> objectPositions) {
float zoom = 1, camX = game.width/2, camY = game.height/2;
boolean isOffScreen = false;
float maxX, minX, maxY, minY;

for (int i = 0; i < objectPositions.size; i++) {
	if (isOffScreen(objectPositions.get(i))) isOffScreen = true;	
}

if (isOffScreen) {
	minX = objectPositions.first().x; maxX = minX;
	minY = objectPositions.first().y; maxY = minY;
	
	for (int i = 0; i < objectPositions.size; i++) {
		Vector2 pos = objectPositions.get(i);
		if (pos.x < minX) minX = pos.x;
		if (pos.x > maxX) maxX = pos.x;
		if (pos.y < minY) minY = pos.y;
		if (pos.y > maxY) maxY = pos.y;				
	}
	
	float padding = 100;
	
	if ((maxX - minX) < (game.width - padding *2) && 
			(maxY - minY) < (game.height - padding *2)) {
		camX = (maxX - minX)/2 + minX; 
		camY = (maxY - minY)/2 + minY; 
	}
	else {
		camX = (maxX - minX)/2 + minX; 
		camY = (maxY - minY)/2 + minY;
		if (game.width / game.height < (maxX - minX) / (maxY - minY)) {
			zoom = (maxX - minX) / (game.width - padding *2);
		}
		else {
			zoom = (maxY - minY) / (game.height - padding *2);
		}
	}
	
}

return new Vector3(camX, camY, zoom);
}

boolean isOffScreen(Vector2 pos) {
if (pos.x < 0 || pos.x > game.width || pos.y < 0 || pos.y > game.height) return true;
else return false;
}
/**/