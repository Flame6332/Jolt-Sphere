package org.joltsphere.mountain;

import org.joltsphere.main.JoltSphereMain;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class MountainObjects {

	private World world;
	private Array<Body> sidewalls;
	private Array<SidePlatform> platforms;
	private int platformCount = 0;
	
	private float ppm = JoltSphereMain.ppm;
	private final int width = JoltSphereMain.WIDTH, height = JoltSphereMain.HEIGHT;
	private float mountainWidth = width * 0.8f;
	private float borderSize = (width - (mountainWidth)) / 2f;
	private float fallingSpeed = 0.4f;
	private float tallestPlatformHeight;
	
	public MountainObjects(World world) {
		this.world = world;
		
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.KinematicBody;
		bdef.position.y = height / ppm;
		
		PolygonShape polygon = new PolygonShape();
		polygon.setAsBox(20 /ppm, height / ppm);
		
		FixtureDef fdef  = new FixtureDef();
		fdef.shape = polygon;
		fdef.friction = 10;
		
		sidewalls = new Array<Body>();
		bdef.position.x = borderSize / ppm;
		sidewalls.add(world.createBody(bdef));
		sidewalls.get(0).createFixture(fdef).setUserData("ground");
		bdef.position.x = (width - borderSize) / ppm;
		sidewalls.add(world.createBody(bdef)); 
		sidewalls.get(1).createFixture(fdef).setUserData("ground");

		sidewalls.get(0).setLinearVelocity(0, -fallingSpeed);
		sidewalls.get(1).setLinearVelocity(0, -fallingSpeed);
		
		polygon.dispose();
		
		/*createLeftFork(0 / ppm, 100/ppm, 1);
		createRightFork(width / ppm, 300/ppm, 1);*/
		platforms = new Array<SidePlatform>();
		
		for (int i = 1; i < 9; i++) {
			addPlatform(false);
		}
		tallestPlatformHeight = platforms.get(platforms.size - 1).body.getPosition().y;
		
	}
	
	private void addPlatform(boolean hasFallingStarted) {
		platformCount++;
		platforms.add(new SidePlatform(platformCount, hasFallingStarted));
	}
	
	public void update(float dt) {
		
		for (int i = 0; i < sidewalls.size; i++) {
			if (sidewalls.get(i).getPosition().y < 0) {
				sidewalls.get(i).setTransform(sidewalls.get(i).getPosition().x, height / ppm, 0);
			}
		}
		
		for (int i = 0; i < platforms.size; i++) {
			platforms.get(i).update();
			if (platforms.get(i).isDead) {
				platforms.removeIndex(i);
				addPlatform(true);
			}
		}
		
	}
	
	private class SidePlatform {
		
		public Body body;
		public boolean isDead = false;
		
		public SidePlatform(int sizeCount, boolean hasFallingStarted) {
			float altitude;
			if (!hasFallingStarted) altitude = (sizeCount * 300 / ppm); // altitude based off of array size 
			else altitude = tallestPlatformHeight;
			
			float scale = (float) (Math.random() *0.5f) + 1.7f; // returns a scale between 1 and 2 then scales it by 0.7
			
			if ((sizeCount%2) == 0) createLeftFork(borderSize / ppm, altitude, scale); // if array size is even 
			else createRightFork((width - borderSize) / ppm, altitude, scale);
		}
		
		private void createLeftFork(float x, float y, float scale) {
			createFork(1, x, y, scale);
		}
		private void createRightFork(float x, float y, float scale) {
			createFork(-1, x, y, scale);
		}
		private void createFork(int dir, float x, float y, float scl) {
			BodyDef bdef = new BodyDef();
			bdef.type = BodyType.KinematicBody;
			bdef.position.x = x;
			bdef.position.y = y;
			bdef.bullet = true;
			
			PolygonShape polygon = new PolygonShape();
			Vector2[] v = new Vector2[4];
			v[0] = new Vector2(0 *scl / ppm, 50*scl / ppm);
			v[1] = new Vector2(800 * dir *scl / ppm, 50*scl / ppm);
			v[2] = new Vector2(800 * dir *scl / ppm, 30*scl / ppm);
			v[3] = new Vector2(0 *scl / ppm, 0*scl / ppm);
			
			polygon.set(v);
			
			FixtureDef fdef  = new FixtureDef();
			fdef.shape = polygon;
			fdef.friction = 1;
			
			body = world.createBody(bdef); 
			body.createFixture(fdef).setUserData("ground");
			body.setLinearVelocity(0, -fallingSpeed);
			
			polygon.dispose();
		}
		
		public void update() {
			if (body.getPosition().y < (100 /ppm)) dispose();
		}
		
		private void dispose() {
			body.destroyFixture(body.getFixtureList().first());
			world.destroyBody(body);
			isDead = true;
		}
		
	}
	
}
 