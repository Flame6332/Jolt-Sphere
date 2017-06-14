package org.joltsphere.mechanics;

import org.joltsphere.main.JoltSphereMain;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class StreamBeamPlayer {
	
	private World world;
	private Body body;
	private Body sightBody;
	private Body rotatingBody;
	private Array<FiredBall> firedBalls;
	private Fixture fixture;
	private Fixture sightFixture;
	@SuppressWarnings("unused")
	private Fixture rotatingFixture;
	private BodyDef bdef;
	private BodyDef bdefFire;
	private FixtureDef fdef;
	private FixtureDef fdefFire;
	private CircleShape circle;
	private CircleShape sightCircle;
	
	private float sightRotationAmount = 0;
	
	private float firingRate = 0.01f; // the smaller the faster
	private float timeLeftUntilNextShot = firingRate;
	
	private float ppm = JoltSphereMain.ppm;
	private float dt = 0.01666f;
	
	public StreamBeamPlayer(World world, int x, int y) {
		this.world = world;
		
		bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(x / ppm, y / ppm);
		bdef.fixedRotation = false;
		bdef.bullet = true;
		body = world.createBody(bdef);
		fdef = new FixtureDef();
		circle = new CircleShape();
		circle.setRadius(50 / ppm);
		fdef.shape = circle;
		fdef.friction = 0.4f;
		fdef.density = 10;
		fdef.restitution = 0.2f;
		fdef.filter.categoryBits = 1;
		fdef.filter.maskBits = 1;
		fixture = body.createFixture(fdef);
		fixture.setUserData("p1");
		
		bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(x / ppm, y / ppm);
		bdef.fixedRotation = true;
		bdef.bullet = false;
		rotatingBody = world.createBody(bdef);
		sightBody = world.createBody(bdef);
		fdef = new FixtureDef();
		sightCircle = new CircleShape();
		sightCircle.setRadius(20 / ppm);
		fdef.friction = 0f;
		fdef.density = 0.001f;
		fdef.restitution = 0f;
		fdef.filter.categoryBits = 2;
		fdef.filter.maskBits = 1;
		fdef.shape = circle;
		rotatingFixture = rotatingBody.createFixture(fdef);
		fdef.shape = sightCircle;
		sightFixture = sightBody.createFixture(fdef);
		sightFixture.setUserData("sight");
		
		WeldJointDef wdef = new WeldJointDef();
		wdef.bodyA = rotatingBody;
		wdef.bodyB = sightBody;
		wdef.localAnchorA.set(50 / ppm, 0);
		wdef.localAnchorB.set(0, 0);
		wdef.collideConnected = false;
		world.createJoint(wdef);
		RevoluteJointDef rdef = new RevoluteJointDef();
		rdef.bodyA = body;
		rdef.bodyB = rotatingBody;
		rdef.localAnchorA.set(0, 0);
		rdef.localAnchorB.set(0,0);
		rdef.collideConnected = false;
		world.createJoint(rdef);
		
		firedBalls = new Array<FiredBall>();
		
		bdefFire = new BodyDef();
		bdefFire.type = BodyType.DynamicBody;
		bdefFire.fixedRotation = false;
		bdefFire.bullet = false;
		fdefFire = new FixtureDef();
		CircleShape fireCircle = new CircleShape();
		fireCircle.setRadius(16 / ppm);
		fdefFire.shape = fireCircle;
		fdefFire.friction = 1f;
		fdefFire.density = 10f;
		fdefFire.restitution = 0.3f;
		fdefFire.filter.categoryBits = 1;
		fdefFire.filter.maskBits = 1;
			
	}
	
	public void update(float dt) {
		this.dt = dt;
		updateFiredBalls();
	}

	public void moveLeft() {
		moveHorizontal(-1);
	}
	public void moveRight() {
		moveHorizontal(1);
	}
	private void moveHorizontal(int dir) {
		
	}
	
	public void rotateAimLeft() {
		rotateAim(-1);
	}
	public void rotateAimRight() {
		rotateAim(1);
	}
	private void rotateAim(int dir) {
		sightRotationAmount -= 0.04f * dir * dt / 0.016666f;
		rotatingBody.setTransform(rotatingBody.getPosition(), sightRotationAmount);
	}
	
	public void fire() {
		if (timeLeftUntilNextShot < 0) {
			bdefFire.position.set(sightBody.getPosition());
			timeLeftUntilNextShot = firingRate;
			firedBalls.add(new FiredBall());
		}
		else timeLeftUntilNextShot -= dt;
	}
	private void updateFiredBalls() {
		for (int i = 0; i < firedBalls.size; i++) {
			firedBalls.get(i).update();
			if (firedBalls.get(i).isDead) firedBalls.removeIndex(i);
		}
	}
	
	private class FiredBall {
		private Body fireBody;
		private float deathCountdown = 1.0f;
		public boolean isDead = false;
		public FiredBall() {
			fireBody = world.createBody(bdefFire);
			fireBody.createFixture(fdefFire).setUserData("fire");
			fireBody.applyLinearImpulse(vectorComponent(
					body.getPosition().x, body.getPosition().y,
					sightBody.getPosition().x, sightBody.getPosition().y, 
					10f), new Vector2(0,0), true);
		}
		public void update() {
			if (deathCountdown < 0) {
				fireBody.destroyFixture(fireBody.getFixtureList().first());
				world.destroyBody(fireBody);
				isDead = true;
			}
			deathCountdown -= dt;
		}
	}

	public Vector2 vectorComponent(float x1, float  y1, float  x2, float y2, float magnitude) {
		
		float xRelativeToFirst = x2 - x1;
		float yRelativeToFirst = y2 - y1;
		
		float pythagifiedLine = (xRelativeToFirst * xRelativeToFirst) + (yRelativeToFirst * yRelativeToFirst);
		
		pythagifiedLine = (float) Math.sqrt(pythagifiedLine);
		
		float percentOfLine = magnitude / pythagifiedLine;
		
		float xIterationSpeed = percentOfLine * xRelativeToFirst; 
		float yIterationSpeed = percentOfLine * yRelativeToFirst;
		
		return new Vector2(xIterationSpeed, yIterationSpeed);
	}
	
}
