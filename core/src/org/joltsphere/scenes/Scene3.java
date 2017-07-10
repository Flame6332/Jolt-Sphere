package org.joltsphere.scenes;

import org.joltsphere.main.JoltSphereMain;
import org.joltsphere.mountain.MountainSpace;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class Scene3 implements Screen {

	final JoltSphereMain game;
	 
	World world;
	Box2DDebugRenderer debugRender;
	
	MountainSpace mountainSpace;
	
	float ppm = JoltSphereMain.ppm;
	
	public Scene3 (final JoltSphereMain gam) {
		game = gam;
		
		world = new World(new Vector2(0, -9.8f), false); //ignore inactive objects false
	
		debugRender = new Box2DDebugRenderer(); 
		
		mountainSpace = new MountainSpace(world); 
		
	} 
	
	private void update(float dt) {
		mountainSpace.update(dt, game.cam.viewportWidth / 2f, game.cam.viewportHeight / 2f);
		world.step(dt, 6, 2);
	}
	
		
	public void render(float dt) {
		update(dt);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		game.shapeRender.begin(ShapeType.Filled);
		
		mountainSpace.shapeRender(game.shapeRender);
		
		game.shapeRender.end();
		
		debugRender.render(world, game.phys2Dcam.combined);
		
		game.batch.begin();
		
		//game.font.draw(game.batch, mountainSpace.points + "boombooms      FPS: " + Gdx.graphics.getFramesPerSecond(), game.width*0.27f, game.height * 0.85f);
		
		game.batch.end();
		
		float zoom = mountainSpace.getZoom(game.cam.viewportHeight);
		game.cam.zoom = zoom;
		game.phys2Dcam.zoom = zoom;
		game.cam.position.set(mountainSpace.getCameraPostion(), 0);
		game.phys2Dcam.position.set(mountainSpace.getDebugCameraPostion(), 0);
		
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