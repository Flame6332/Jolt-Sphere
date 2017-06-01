package org.joltsphere.testing.scenes;

import org.joltsphere.testing.main.JoltSphereTesting;
import org.joltsphere.testing.mechanics.TestEntities;
import org.joltsphere.testing.mechanics.TestPlayer;
import org.joltsphere.testing.mechanics.TestingContactListener;

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

	final JoltSphereTesting game;
	 
	World world;
	TestingContactListener contLis;
	Box2DDebugRenderer debugRender;
	TestEntities ent;
	
	TestPlayer player1;
	TestPlayer player2;
	
	float ppm = JoltSphereTesting.ppm;
	
	public Scene1 (final JoltSphereTesting gam) {
		game = gam;
		
		world = new World(new Vector2(0, -9.8f), false); //ignore inactive objects false
		contLis = new TestingContactListener();
		world.setContactListener(contLis);
		
		debugRender = new Box2DDebugRenderer(); 
		
		ent = new TestEntities();
		
		ent.createPlatform(world);
		world = ent.world;
		
		int x = 300;
		player1 = new TestPlayer(game.width/2 + x, 300, world, 1);
		player2 = new TestPlayer(game.width/2 - x, 300, world, 2);
		
	} 
	
	void update(float dv) {
		if (Gdx.input.isKeyJustPressed(Keys.TAB)) game.switchScene();
		
		player1.update(contLis.player1Contact, dv, game.width, game.height);
		player2.update(contLis.player2Contact, dv, game.width, game.height);
		
		if (Gdx.input.isKeyPressed(Keys.LEFT)) player1.moveLeft();
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) player1.moveRight();
		if (Gdx.input.isKeyJustPressed(Keys.UP)) player1.jump();
			if (Gdx.input.isKeyPressed(Keys.UP)) player1.jumpHold();
		if (Gdx.input.isKeyPressed(Keys.DOWN)) player1.smash(); else player1.notSmashing();
		
		if (Gdx.input.isKeyPressed(Keys.A)) player2.moveLeft();
		if (Gdx.input.isKeyPressed(Keys.D)) player2.moveRight();
		if (Gdx.input.isKeyJustPressed(Keys.W)) player2.jump();
			if (Gdx.input.isKeyPressed(Keys.W)) player2.jumpHold();
		if (Gdx.input.isKeyPressed(Keys.S)) player2.smash(); else player2.notSmashing();
		
		if (player1.wasKnockedOut) {
			player1.wasKnockedOut = false;
			player2.otherPlayerKnockedOut();
		}
		if (player2.wasKnockedOut) {
			player2.wasKnockedOut = false;
			player1.otherPlayerKnockedOut();
		}
		
	}
	
	public void render(float delta) {
		update(delta);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		game.cam.update();
		game.phys2Dcam.update();
		world.step(delta, 6, 2);
		
		game.batch.begin();
			game.font.draw(game.batch, "" + player1.knockouts, game.width*0.27f, game.height * 0.085f);
			game.font.draw(game.batch, "" + player2.knockouts, game.width*0.72f, game.height * 0.085f);
			
			if (player1.canSmash) game.font.draw(game.batch, "Jolt! < ^ >", game.width * 0.85f, game.height * 0.1f);
			if (player2.canSmash) game.font.draw(game.batch, "Jolt! WASD", game.width * 0.05f, game.height * 0.1f);
		
			game.font.draw(game.batch, Math.round((player1.fdefBall.density / 5 * 100) * 10f)/10f + "%", game.width * 0.85f, game.height * 0.6f);
			game.font.draw(game.batch, Math.round((player2.fdefBall.density / 5 * 100) * 10f)/10f + "%", game.width * 0.06f, game.height * 0.6f);
			
			
		game.batch.end();
		
		game.shapeRender.begin(ShapeType.Filled);
			
			player1.shapeRender(game.shapeRender, Color.FIREBRICK);
			player2.shapeRender(game.shapeRender, Color.BLUE);
			
		game.shapeRender.end();
		
		debugRender.render(world, game.phys2Dcam.combined);
		
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