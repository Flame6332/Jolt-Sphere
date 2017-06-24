package org.joltsphere.mountain;

import org.joltsphere.main.JoltSphereMain;
import org.joltsphere.mechanics.MountainClimbingPlayer;
import org.joltsphere.mechanics.StreamBeamPlayer;
import org.joltsphere.misc.EllipseFixture;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
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
	
	private Array<Body> sidewalls;
	private RisingPlatform risingPlatform;
	private Array<Platform> platforms;
	private int platformCount = 0;
	
	private int streamBeamGroundContacts = 0, mountainClimberGroundContacts = 0;
	
	private float ppm = JoltSphereMain.ppm;
	private final int width = JoltSphereMain.WIDTH, height = JoltSphereMain.HEIGHT;
	private float mountainWidth = width;
	private float risingSpeed = 0.7f;
	private float viewHalfWidth, viewHalfHeight;
	
	public MountainSpace(World world) {
		this.world = world;
		world.setContactListener(new ContLis());
		streamBeam = new StreamBeamPlayer(world, width/2f, height/2f, Color.RED);
		mountainClimber = new MountainClimbingPlayer(world, width/2f, height/2f, Color.BLUE);
		
		risingPlatform = new RisingPlatform();
		
		sidewalls = new Array<Body>();
		
		platforms = new Array<Platform>();
		
		viewHalfWidth = width/2f; viewHalfHeight = height/2f;
	}
	public void shapeRender(ShapeRenderer shapeRender) {
		streamBeam.shapeRender(shapeRender);
		mountainClimber.shapeRender(shapeRender);
	}
	public void update(float dt, float viewHalfWidth, float viewHalfHeight) {
		this.viewHalfWidth = viewHalfWidth; this.viewHalfHeight = viewHalfHeight;
		
		streamBeam.input(Keys.UP, Keys.DOWN, Keys.LEFT, Keys.RIGHT, Keys.K, Keys.SEMICOLON, Keys.O);
		mountainClimber.input(Keys.W, Keys.S, Keys.A, Keys.D, Keys.SHIFT_LEFT, false);
		streamBeam.update(dt, streamBeamGroundContacts);
		mountainClimber.update(dt, mountainClimberGroundContacts);
	}
	
	private class ContLis implements ContactListener {
		private Fixture fa, fb;
		
		public void beginContact(Contact contact) {	
			fa = contact.getFixtureA();	fb = contact.getFixtureB();
			
			if (isContacting("mountainClimber", "ground")) mountainClimberGroundContacts++;
			if (isContacting("streamBeam", "ground")) streamBeamGroundContacts++;
			if (isContacting("deathPlatform", "streamBeam"));
			if (isContacting("deathPlatform", "mountainClimber"));
		}
		public void endContact(Contact contact) {
			fa = contact.getFixtureA(); fb = contact.getFixtureB();
			
			if (isContacting("mountainClimber", "ground")) mountainClimberGroundContacts--;
			if (isContacting("streamBeam", "ground")) streamBeamGroundContacts--;	
		}
		public void preSolve(Contact contact, Manifold oldManifold) {}
		public void postSolve(Contact contact, ContactImpulse impulse) {}
		private boolean isContacting(String f1, String f2) {
			if (fa.getUserData() == f1 && fb.getUserData() == f2) return true;
			else if (fa.getUserData() == f2 && fb.getUserData() == f1) return true;
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
			fdef.friction = 10;
			fdef.restitution = 0;
			body = world.createBody(bdef);
			body.createFixture(fdef).setUserData("deathPlatform");
			body.setLinearVelocity(0, risingSpeed);
			polygon.dispose();
		}
	}
	
	private class Platform {
		
		public Platform() {
			
		}
		
	}
	
	public Vector2 getDebugCameraPostion() {
		return new Vector2(width / 2f / ppm, (streamBeam.body.getPosition().y + mountainClimber.body.getPosition().y) / 2f);
	}
	public Vector2 getCameraPostion() {
		return new Vector2(getDebugCameraPostion().x * ppm, getDebugCameraPostion().y * ppm);
	}
}
 