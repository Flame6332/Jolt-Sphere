package org.joltsphere.mountain;

import org.joltsphere.main.JoltSphereMain;
import org.joltsphere.mechanics.MountainClimbingPlayer;
import org.joltsphere.mechanics.StreamBeamPlayer;
import org.joltsphere.misc.EllipseFixture;
import org.joltsphere.misc.Misc;
import org.joltsphere.misc.ObjectData;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class MountainSpace {

	private World world;
	private StreamBeamPlayer streamBeam;
	private MountainClimbingPlayer mountainClimber;
	
	private Array<SideWall> sideWalls;
	private RisingPlatform risingPlatform;
	private Array<Platform> platforms;
	//private Array<Body> fixturesToBeDestroyed;
	private float highestPlatform = 0;
	private float tallestSideWall = 0;
	private float platformPopulationDensity = 150;
	private float risingPlatformHeight;
	private float sideWallCount = 1;
	
	public int points = 0;
	private int streamBeamGroundContacts = 0, mountainClimberGroundContacts = 0;
	
	private float ppm = JoltSphereMain.ppm;
	private final int width = JoltSphereMain.WIDTH, height = JoltSphereMain.HEIGHT;
	private float mountainWidth = width;
	private float borderSize = (width - mountainWidth) / 2f;
	private float risingSpeed = 0.7f;
	
	public MountainSpace(World world) {
		this.world = world;
		world.setContactListener(new ContLis());
		streamBeam = new StreamBeamPlayer(world, width/2f, height, Color.RED);
		mountainClimber = new MountainClimbingPlayer(world, width/2f, height, Color.BLUE);
		
		risingPlatform = new RisingPlatform();
		sideWalls = new Array<SideWall>();
		platforms = new Array<Platform>();
		//fixturesToBeDestroyed = new Array<Body>();
		
	} 	
	public void shapeRender(ShapeRenderer shapeRender) {
		streamBeam.shapeRender(shapeRender);
		mountainClimber.shapeRender(shapeRender);
		
		//shapeRender.setColor((float) Math.sin(risingPlatformHeight), (float) Math.cos(risingPlatformHeight), (float) Math.tan(risingPlatformHeight), 1);
		//shapeRender.rect(0, 0, width, risingPlatformHeight*ppm +20);
		
		shapeRender.setColor(193/255f, 138/255f, 0, 0.24f);
		//shapeRender.setColor(Color.RAINBOW);
		shapeRender.rect(0, 0, width, risingPlatformHeight*ppm +20);
		/*shapeRender.setColor(193/255f, 138/255f, 0, 0.24f);
		shapeRender.rect(0, 0, width, risingPlatformHeight*ppm +20);
		shapeRender.setColor(Color.BROWN);
		for (int i = 1; i <= 7; i++) {
			shapeRender.ellipse(Misc.random(0, 100) +i*(width / 8)  + 50, risingPlatformHeight*ppm, Misc.random(100, 200), Misc.random(30, 90));
		}*/
	}
	public void update(float dt, float viewHalfWidth, float viewHalfHeight) {
		risingPlatformHeight = risingPlatform.getHeight();
		viewHalfHeight = viewHalfHeight*2*getZoom(viewHalfHeight*2);
		
		streamBeam.input(Keys.UP, Keys.DOWN, Keys.LEFT, Keys.RIGHT, Keys.K, Keys.SEMICOLON, Keys.O);
		mountainClimber.input(Keys.W, Keys.S, Keys.A, Keys.D, Keys.SHIFT_LEFT, false);
		streamBeam.update(dt, streamBeamGroundContacts);
		mountainClimber.update(dt, mountainClimberGroundContacts);
		
		while ((getCameraPostion().y + viewHalfHeight) > highestPlatform) {
			highestPlatform += platformPopulationDensity;
			platforms.add(new Platform());
		}
		for (Platform platform : platforms) {
			if (platform.isDead()) platforms.removeValue(platform, true);
		}
		if ((getCameraPostion().y + viewHalfHeight) > tallestSideWall) {
			sideWalls.add(new SideWall());
			sideWallCount++;
			for (SideWall sideWall : sideWalls) {
				if (sideWall.isDead) sideWalls.removeValue(sideWall, true); 
			}
		}
		
		/*for (Body f : fixturesToBeDestroyed) {
			/*System.out.println(f.getUserData());
			fixturesToBeDestroyed.removeValue(f, true);
			System.out.println("removed value");
			if (f.getUserData() != null) world.destroyBody(f);
			System.out.println("fixture destroyed");
		}*/
	
	}
	
	private class ContLis implements ContactListener {
		private Body bA, bB;
		public void beginContact(Contact contact) {	
			bA = contact.getFixtureA().getBody();	bB = contact.getFixtureB().getBody();
			if (isContacting("mountainClimber", "ground")) mountainClimberGroundContacts++;
			if (isContacting("streamBeam", "ground")) streamBeamGroundContacts++;
			if (isContacting("deathPlatform", "streamBeam"));
			if (isContacting("deathPlatform", "mountainClimber"));
			if (isContacting("fire", "mountainClimber")) {
				if (((ObjectData) bA.getUserData()).string == "fire") streamBeam.firedBallsToBeRemoved.add(((ObjectData) bA.getUserData()).count);
				else streamBeam.firedBallsToBeRemoved.add(((ObjectData) bB.getUserData()).count);
				points++;
			}
		}
		public void endContact(Contact contact) {
			bA = contact.getFixtureA().getBody(); bB = contact.getFixtureB().getBody();
			if (isContacting("mountainClimber", "ground")) mountainClimberGroundContacts--;
			if (isContacting("streamBeam", "ground")) streamBeamGroundContacts--;
		}
		public void preSolve(Contact contact, Manifold oldManifold) {}
		public void postSolve(Contact contact, ContactImpulse impulse) {}
		private boolean isContacting(String string1, String string2) {
			String dat1, dat2;
			if (bA.getUserData() instanceof ObjectData) {
				dat1 = ((ObjectData) bA.getUserData()).string;
			}
			else dat1 = (String) bA.getUserData();
			if (bB.getUserData() instanceof ObjectData) {
				dat2 = ((ObjectData) bB.getUserData()).string;
			}
			else dat2 = (String) bB.getUserData();
			
			if (dat1 == string1 && dat2 == string2) return true;
			else if (dat1 == string2 && dat2==string1) return true;
			else return false;
		}
	}
	
	private class RisingPlatform {
		Body body; 
		public RisingPlatform() {
			BodyDef bdef = new BodyDef();
			bdef.type = BodyType.KinematicBody;
			bdef.position.x = width/2f / ppm;
			bdef.position.y = -40 / ppm;
			PolygonShape polygon = new PolygonShape();
			polygon.setAsBox(mountainWidth/2f /ppm, 20 / ppm);
			FixtureDef fdef  = new FixtureDef();
			fdef.shape = polygon;
			fdef.friction = 3;
			fdef.restitution = 0;
			body = world.createBody(bdef);
			body.createFixture(fdef).setUserData("deathPlatform");
			body.setLinearVelocity(0, risingSpeed);
			polygon.dispose();
		}
		public float getHeight() {
			return body.getPosition().y;
		}
	}
	
	private class Platform {
		
		private Body body;
		
		public Platform() {
			BodyDef bdef = new BodyDef();
			bdef.type = BodyType.StaticBody;
			bdef.position.set(Misc.random() * mountainWidth / ppm, highestPlatform / ppm);
			body = world.createBody(bdef);
			body.setUserData("ground");
			float size = 400f;
			EllipseFixture.createEllipseFixtures(body, 1, 0, 1, Misc.random(0.1f, 1f) * size /ppm, Misc.random(0.1f, 1f) * size /ppm, null);
		}
		public boolean isDead() {
			if (body.getPosition().y < risingPlatformHeight) {
				world.destroyBody(body);
				return true;
			} else return false;
		}
	}
	
	private class SideWall {
		
		private Body leftBody;
		private Body rightBody;
		public boolean isDead = false;
		
		public SideWall() {
			BodyDef bdef = new BodyDef();
			bdef.type = BodyType.StaticBody;
			PolygonShape polygon = new PolygonShape();
			polygon.setAsBox(20 / ppm, height / ppm);
			FixtureDef fdef  = new FixtureDef();
			fdef.shape = polygon;
			fdef.friction = 10;
			fdef.restitution = 0;
			bdef.position.set(borderSize, height*sideWallCount / ppm);
			leftBody = world.createBody(bdef);
			bdef.position.x = (borderSize + mountainWidth) / ppm;
			rightBody = world.createBody(bdef);
			leftBody.createFixture(fdef).setUserData("");
			rightBody.createFixture(fdef).setUserData("");
			polygon.dispose();
			tallestSideWall = height*sideWallCount + height;
		}
		
	}
	
	public float getZoom(float viewPortHeight) {
		float pad = 1.2f; // amount of padding so it isnt on the border all the time
		if (viewPortHeight > Math.abs(streamBeam.body.getPosition().y - mountainClimber.body.getPosition().y)*pad*ppm)
			return 1;
		else 
			return Math.abs(streamBeam.body.getPosition().y - mountainClimber.body.getPosition().y)*pad*ppm / viewPortHeight;
	}
	public Vector2 getDebugCameraPostion() {
		return new Vector2(width / 2f / ppm, (streamBeam.body.getPosition().y + mountainClimber.body.getPosition().y) / 2f);
	}
	public Vector2 getCameraPostion() {
		return new Vector2(getDebugCameraPostion().x * ppm, getDebugCameraPostion().y * ppm);
	}
}
 