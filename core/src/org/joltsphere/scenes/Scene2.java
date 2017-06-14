package org.joltsphere.scenes;

import org.joltsphere.main.JoltSphereMain;
import org.joltsphere.mechanics.WorldEntities;
import org.joltsphere.mechanics.StreamBeamContactListener;
import org.joltsphere.mechanics.StreamBeamPlayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class Scene2 implements Screen {

	final JoltSphereMain game;
	 
	World world;
	Box2DDebugRenderer debugRender;
	StreamBeamContactListener contLis;
	WorldEntities ent;
	
	StreamBeamPlayer player;
	
	float ppm = JoltSphereMain.ppm;
	
	public Scene2 (final JoltSphereMain gam) {
		game = gam;
		
		world = new World(new Vector2(0, -9.8f), false); //ignore inactive objects false
	
		debugRender = new Box2DDebugRenderer(); 
		
		ent = new WorldEntities();
		contLis = new StreamBeamContactListener();
		
		ent.createFlatPlatform(world);
		world = ent.world;
		world.setContactListener(contLis);
		
		player = new StreamBeamPlayer(world, 200, 200);
		
	} 
	
	private void update(float dt) {
		if (Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
			if (Gdx.input.isKeyPressed(Keys.LEFT)) player.rotateAimLeft();
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) player.rotateAimRight();
			if (Gdx.input.isKeyPressed(Keys.UP)) player.fire();
		}
		else {
			
		}
		
	}
	
		
	public void render(float dt) {
		update(dt);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		world.step(dt, 6, 2);
	
		player.update(dt);
		
		game.shapeRender.begin(ShapeType.Filled);
		
		//nothin yet m8
			
		game.shapeRender.end();
		
		debugRender.render(world, game.phys2Dcam.combined);
		
		game.batch.begin();
		
		//game.font.draw(game.batch, "" + arena.players.get(0).knockouts, game.width*0.27f, game.height * 0.085f);
		
		game.batch.end();
			
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