package org.joltsphere.scenes;

import org.joltsphere.main.JoltSphereMain;
import org.joltsphere.mechanics.WorldEntities;
import org.joltsphere.mechanics.ArenaPlayer;
import org.joltsphere.mechanics.ArenaSpace;
import org.joltsphere.mechanics.ArenaContactListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Scene1 implements Screen {

	final JoltSphereMain game;
	 
	World world;
	ArenaContactListener contLis;
	Box2DDebugRenderer debugRender;
	WorldEntities ent;
	
	ArenaSpace arena;
	
	Array<Vector2> objectPositions; 
	Vector2 worldCenter;
	
	float camZoom = 1;
	float ppm = JoltSphereMain.ppm;
	
	public Scene1 (final JoltSphereMain gam) {
		game = gam;
		
		world = new World(new Vector2(0, -9.8f), false); //ignore inactive objects false
		
		Gdx.input.setInputProcessor(new GameInput());
		
		debugRender = new Box2DDebugRenderer(); 
		
		ent = new WorldEntities();
		arena = new  ArenaSpace(game.width, game.height);
		
		ent.createPlatform(world);
		world = ent.world;
		
		int x = 300;
		arena.players.add(new ArenaPlayer(game.width/2 + x, 300, world, 1, Color.FIREBRICK));
		arena.players.add(new ArenaPlayer(game.width/2 - x, 300, world, 2, Color.BLUE));
		
		contLis = new ArenaContactListener(arena.players.size);
		world.setContactListener(contLis);
		
		worldCenter = new Vector2(game.width/2, 300);
		objectPositions = new Array<Vector2>();
		
	} 
	
	void update(float dv) {
		if (Gdx.input.isKeyJustPressed(Keys.TAB)) game.switchScene();
		
		if (Gdx.input.isKeyPressed(Keys.DOWN)) arena.players.get(0).smash(); 
			else arena.players.get(0).notSmashing(); //place at top for smash density
		if (Gdx.input.isKeyPressed(Keys.LEFT)) arena.players.get(0).moveLeft();
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) arena.players.get(0).moveRight();
		if (Gdx.input.isKeyPressed(Keys.UP)) arena.players.get(0).jumpHold();
		
		if (Gdx.input.isKeyPressed(Keys.S)) arena.players.get(1).smash(); 
			else arena.players.get(1).notSmashing(); //place at top for smash density
		if (Gdx.input.isKeyPressed(Keys.A)) arena.players.get(1).moveLeft();
		if (Gdx.input.isKeyPressed(Keys.D)) arena.players.get(1).moveRight();
		if (Gdx.input.isKeyPressed(Keys.W)) arena.players.get(1).jumpHold();
		
		arena.update(dv, contLis.playerContacts);
		
		if (contLis.pvpContact > 0) {
			arena.players.get(0).contactingOtherPlayer();
			arena.players.get(1).contactingOtherPlayer();
		} else {
			arena.players.get(0).notContactingOtherPlayer();
			arena.players.get(1).notContactingOtherPlayer();
		}
		
	}
	
	private class GameInput implements InputProcessor {

		@Override
		public boolean keyDown(int keycode) {
			
			if (keycode == Keys.UP) arena.players.get(0).jump();
			if (keycode == Keys.W) arena.players.get(1).jump();
			
			return false;
		}

		@Override
		public boolean keyUp(int keycode) {
			return false;
		}

		@Override
		public boolean keyTyped(char character) {
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			return false;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			return false;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			return false;
		}

		@Override
		public boolean scrolled(int amount) {
			return false;
		}
		
	}
		
	public void render(float dv) {
		update(dv);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		world.step(dv, 6, 2);
	
		game.shapeRender.begin(ShapeType.Filled);
			
			for (int i = 1; i < arena.players.get(0).trail.size; i++) {
				game.shapeRender.setColor(arena.players.get(0).color);
				game.shapeRender.circle(arena.players.get(0).trail.get(i).x * ppm, arena.players.get(0).trail.get(i).y * ppm, 50);
	
				game.shapeRender.setColor(arena.players.get(1).color);
				game.shapeRender.circle(arena.players.get(1).trail.get(i).x * ppm, arena.players.get(1).trail.get(i).y * ppm, 50);
			}/**/
		
			arena.players.get(0).shapeRender(game.shapeRender);
			arena.players.get(1).shapeRender(game.shapeRender);
			
			game.shapeRender.rect(-10000 - game.width/2f, game.height * 1.5f, 20000, 200, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
			game.shapeRender.rect(-10000 - game.width/2f, game.height * -0.5f - 200, 20000, 200, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
			
		game.shapeRender.end();
		
		debugRender.render(world, game.phys2Dcam.combined);
		
		game.batch.begin();
		game.font.draw(game.batch, "" + arena.players.get(0).knockouts, game.width*0.27f, game.height * 0.085f);
		game.font.draw(game.batch, "" + arena.players.get(1).knockouts, game.width*0.72f, game.height * 0.085f);
		
		if (arena.players.get(0).canSmash) game.font.draw(game.batch, "Jolt! < ^ >", game.width * 0.85f, game.height * 0.1f);
		if (arena.players.get(1).canSmash) game.font.draw(game.batch, "Jolt! WASD", game.width * 0.05f, game.height * 0.1f);
	
		//game.font.draw(game.batch, Math.round((arena.players.get(0).fdefBall.density / 5 * 100) * 10f)/10f + "%", game.width * 0.85f, game.height * 0.6f);
		game.font.draw(game.batch, Math.round((arena.players.get(0).energyTimer * 100) * 10f)/10f + "%", game.width * 0.85f, game.height * 0.6f);
		//game.font.draw(game.batch, Math.round((arena.players.get(1).fdefBall.density / 5 * 100) * 10f)/10f + "%", game.width * 0.06f, game.height * 0.6f);
		game.font.draw(game.batch, Math.round((arena.players.get(1).energyTimer * 100) * 10f)/10f + "%", game.width * 0.06f, game.height * 0.6f);
		
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