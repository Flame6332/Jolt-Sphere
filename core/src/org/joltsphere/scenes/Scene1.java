package org.joltsphere.scenes;

import org.joltsphere.main.JoltSphereMain;
import org.joltsphere.mechanics.WorldEntities;
import org.joltsphere.mechanics.ArenaPlayer;
import org.joltsphere.mechanics.ArenaSpace;
import org.joltsphere.mechanics.ArenaContactListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class Scene1 implements Screen {

	final JoltSphereMain game;
	 
	World world;
	ArenaContactListener contLis;
	Box2DDebugRenderer debugRender;
	WorldEntities ent;
	
	ArenaSpace arena;
	
	Vector2 worldCenter;
	
	float timer = 0;
	boolean isTimerEnabled = true;
	float ppm = JoltSphereMain.ppm;
	
	/* TODO
	 * - Move contact listener and world stuff into the arenaspace for easier world stuff
	 * - Remove unused variables
	 * - Change up player addition to accomodate for previous change 
	 */
	
	public Scene1 (final JoltSphereMain gam) {
		game = gam;
		
		world = new World(new Vector2(0, -9.8f), false); //ignore inactive objects false
	
		debugRender = new Box2DDebugRenderer(); 
		
		ent = new WorldEntities();
		arena = new  ArenaSpace(game.width, game.height);
		
		ent.createPlatform1(world);
		world = ent.world;
		
		int x = 300;
		arena.players.add(new ArenaPlayer(game.width/2 + x, 300, world, 1, Color.FIREBRICK));
		arena.players.add(new ArenaPlayer(game.width/2 - x, 300, world, 2, Color.BLUE));
		
		contLis = new ArenaContactListener(arena.players.size);
		world.setContactListener(contLis);
		
		worldCenter = new Vector2(game.width/2, 300);
		
	} 
	
	void update(float dt) {
	
		arena.input(0, Keys.UP, Keys.DOWN, Keys.LEFT, Keys.RIGHT, Keys.CONTROL_RIGHT);
		arena.input(1, Keys.W, Keys.S, Keys.A, Keys.D, Keys.SHIFT_LEFT); 
		arena.update(dt, contLis.playerContacts);
		
		if (Gdx.input.isKeyJustPressed(Keys.P)) {
			isTimerEnabled = true;
			arena.players.get(0).knockouts = 0;
			arena.players.get(1).knockouts = 0;
		}
		
		if (isTimerEnabled) {
			if (timer < 0) {
				isTimerEnabled = false;			
				timer = 5*60;
			}
			timer-=dt;
		}
		
		if (contLis.pvpContact > 0) {
			arena.players.get(0).contactingOtherPlayer(arena.players.get(1).isSmashing);
			arena.players.get(1).contactingOtherPlayer(arena.players.get(0).isSmashing);
		} else {
			arena.players.get(0).notContactingOtherPlayer(arena.players.get(1).isSmashing);
			arena.players.get(1).notContactingOtherPlayer(arena.players.get(0).isSmashing);
		}
		
		/*if (Controllers.getControllers().get(0).getButton(XBox360.BUTTON_A)) {
			if (game.getControllers(0).getButton(game.BUTTON_A)) {
				Controllers.getControllers().get(0).
				arena.players.get(0).body.applyForceToCenter(1000, 0, true);
			}
		}*/
		
	}
	
		
	public void render(float dt) {
		update(dt);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		world.step(dt, 6, 2);
	
		game.shapeRender.begin(ShapeType.Filled);
			
			arena.shapeRender(game.shapeRender);
			
		game.shapeRender.end();
		
		debugRender.render(world, game.phys2Dcam.combined);
		
		game.batch.begin();
		
		game.font.draw(game.batch, "" + arena.players.get(0).knockouts, game.width*0.27f, game.height * 0.085f);
		game.font.draw(game.batch, "" + arena.players.get(1).knockouts, game.width*0.72f, game.height * 0.085f);
		
		if (arena.players.get(0).canAttack) game.font.draw(game.batch, "Jolt! < ^ >", game.width * 0.85f, game.height * 0.1f);
		if (arena.players.get(1).canAttack) game.font.draw(game.batch, "Jolt! WASD", game.width * 0.05f, game.height * 0.1f);
		
		if (isTimerEnabled) game.font.draw(game.batch, (int)(timer / 60) + " : " + Math.round(((timer / 60) - (int) (timer / 60)) *60), game.width * 0.46f, game.height * 0.92f);	
		
			//game.font.draw(game.batch, Math.round((arena.players.get(0).fdefBall.density / 5 * 100) * 10f)/10f + "%", game.width * 0.85f, game.height * 0.6f);
			//game.font.draw(game.batch, Math.round((arena.players.get(0).energyTimer * 100) * 10f)/10f + "%", game.width * 0.85f, game.height * 0.6f);
		
		game.font.draw(game.batch, Math.round((arena.players.get(0).currentRecievingSmashRestitution) * 10f)/10f + "x", game.width * 0.85f, game.height * 0.6f);
		game.font.draw(game.batch, Math.round((arena.players.get(1).currentRecievingSmashRestitution) * 10f)/10f + "x", game.width * 0.06f, game.height * 0.6f);
		
		game.batch.end();
	
		
		/*objectPositions = new Array<Vector2>();
		objectPositions.add(arena.players.get(1).getPosition());
		objectPositions.add(arena.players.get(1).getPosition());
		objectPositions.add(worldCenter);
		Vector3 pos = findZoom(objectPositions);
		
		//game.cam.position.set(pos.x, pos.y, 0);
		//game.cam.zoom = pos.z;
		//game.phys2Dcam.position.set(pos.x/ppm, pos.y/ppm, 0);
		//game.phys2Dcam.zoom = pos.z;
		*/
		
		game.cam.update();
		game.phys2Dcam.update();
		
	}
	
	public void dispose() {
		
	}
	
	public void resize(int width, int height) {
		game.resize(width, height);
	}
	
	public void show() {}

	public void pause() {}

	public void resume() {}

	public void hide() {}
	
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