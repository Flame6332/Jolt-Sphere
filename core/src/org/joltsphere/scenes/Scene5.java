package org.joltsphere.scenes;

import org.joltsphere.main.JoltSphereMain;
import org.joltsphere.mechanics.WorldEntities;
import org.joltsphere.mechanics.MapBodyBuilder;
import org.joltsphere.mechanics.MountainClimbingPlayer;
import org.joltsphere.mechanics.StreamBeamContactListener;
import org.joltsphere.mechanics.StreamBeamTowedPlayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;

public class Scene5 implements Screen {

	final JoltSphereMain game;
	 
	World world;
	Box2DDebugRenderer debugRender;
	StreamBeamContactListener contLis;
	WorldEntities ent;
	    
	StreamBeamTowedPlayer streamBeam;
	MountainClimbingPlayer otherPlayer;
	
	TiledMap map;
	MapProperties mapProp;
	int mapWidth, mapHeight;
	
	float ppm = JoltSphereMain.ppm;
	
	public Scene5 (final JoltSphereMain gam) {
		game = gam;
		
		world = new World(new Vector2(0, -9.8f), false); //ignore inactive objects false
	
		debugRender = new Box2DDebugRenderer(); 
		
		ent = new WorldEntities();
		contLis = new StreamBeamContactListener();
		
		ent.createFlatPlatform(world);
		world = ent.world;
		world.setContactListener(contLis);
		
		try {
			map = new TmxMapLoader().load("testing/testmap.tmx");
		}
		catch (Exception e) {
			System.out.println("Sumthin Broke");
		}
		
		mapProp = map.getProperties();
		mapWidth = (int) mapProp.get("width", Integer.class) * 320;
		mapHeight = (int) mapProp.get("height", Integer.class) * 320;
		MapBodyBuilder.buildShapes(map, ppm, world, "terrain");
		
		streamBeam = new StreamBeamTowedPlayer(world, 450, (int) (mapHeight/1.8f), Color.RED);
		otherPlayer = new MountainClimbingPlayer(world, 500, (int) (mapHeight/1.8f), Color.BLUE);
		
		DistanceJointDef dDef = new DistanceJointDef();
		dDef.collideConnected = true;
		dDef.bodyA = streamBeam.body; 
		dDef.length = 150 / ppm;
		dDef.frequencyHz = 3f;
		dDef.dampingRatio = 0.4f;
		dDef.bodyB = otherPlayer.body;
		dDef.localAnchorA.set(0,0);
		dDef.localAnchorB.set(0, 0);
		world.createJoint(dDef);
		
	} 
	
	private void update(float dt) {
		streamBeam.input(Keys.P, Keys.P, Keys.P, Keys.P, Keys.LEFT, Keys.RIGHT, Keys.UP);
		otherPlayer.input(Keys.W, Keys.S, Keys.A, Keys.D, Keys.SHIFT_LEFT, true);
	}
	
		
	public void render(float dt) {
		update(dt);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		world.step(dt, 6, 2);
	
		streamBeam.update(dt, 1);
		otherPlayer.update(dt, 1);
		
		game.shapeRender.begin(ShapeType.Filled);
		
		streamBeam.shapeRender(game.shapeRender);
		otherPlayer.shapeRender(game.shapeRender);
			
		game.shapeRender.end();
		
		debugRender.render(world, game.phys2Dcam.combined);
		
		game.batch.begin();
		
		game.font.draw(game.batch, "" + Gdx.graphics.getFramesPerSecond(), game.width*0.27f, game.height * 0.85f);
		
		game.batch.end();
			
		game.cam.position.x = otherPlayer.body.getPosition().x * ppm;
		game.cam.position.y = otherPlayer.body.getPosition().y * ppm;
		game.phys2Dcam.position.x = otherPlayer.body.getPosition().x;
		game.phys2Dcam.position.y = otherPlayer.body.getPosition().y;
		game.cam.zoom = 1;
		game.phys2Dcam.zoom = 1;
		
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