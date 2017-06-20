package org.joltsphere.mechanics;

import org.joltsphere.main.JoltSphereMain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
	public Body body;
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
	
	private Color color;
	public float density() {return 10f;};
	
	private boolean isGrounded = false;
	
	private float firingRate = 0.016667f; // the smaller the faster
	private float ballsTryingToBeFired = 0;
	
	private float sightRotationAmount = 0;
	private boolean didRotateLastFrame = false;
	private float rotationSpeed = 0.005f; // beginning rotation speed
	private float currentRotationSpeed = rotationSpeed;
	
	private float ppm = JoltSphereMain.ppm;
	private float dt = 0.01666f;
	
	public StreamBeamPlayer(World world, int x, int y, Color color) {
		this.world = world;
		this.color = color;
		
		bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(x / ppm, y / ppm);
		bdef.fixedRotation = false;
		bdef.bullet = true;
		bdef.linearDamping = 0.2f;
		bdef.angularDamping = 0.5f;
		body = world.createBody(bdef);
		fdef = new FixtureDef();
		circle = new CircleShape();
		circle.setRadius(50 / ppm);
		fdef.shape = circle;
		fdef.friction = 0.1f;
		fdef.density = density();
		fdef.restitution = 0;
		fdef.filter.categoryBits = 1;
		fdef.filter.maskBits = 1;
		fixture = body.createFixture(fdef);
		fixture.setUserData("streamBeam");
		
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
		bdefFire.bullet = false	;
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
	
	public void shapeRender(ShapeRenderer shapeRender) {
		shapeRender.setColor(color);
		shapeRender.circle(body.getPosition().x*ppm,body.getPosition().y*ppm, 50);
		shapeRender.setColor(Color.GOLD);
		for (int i = 0; i < firedBalls.size; i++) {
			shapeRender.circle(firedBalls.get(i).fireBody.getPosition().x*ppm, firedBalls.get(i).fireBody.getPosition().y*ppm, 16);
		}
		shapeRender.setColor(Color.WHITE);
		shapeRender.circle(sightBody.getPosition().x * ppm, sightBody.getPosition().y * ppm, 20);
	}
	
	public void update(float dt, int groundContacts) {
		this.dt = dt;
		updateFiredBalls();
		
		if (groundContacts > 0) isGrounded = true;
		else isGrounded = false;
		
	}

	public void moveLeft() {
		moveHorizontal(-1);
	}
	public void moveRight() {
		moveHorizontal(1);
	}
	private void moveHorizontal(int dir) {
		body.applyForceToCenter(30*dir, 0, true);
		body.applyTorque(-5 * dir, true);
	}
	public void jump() {
		if (isGrounded) {
			body.setLinearVelocity(body.getLinearVelocity().x * 0.3f, body.getLinearVelocity().y * 0.3f);
			body.applyLinearImpulse(new Vector2(0, 16), body.getPosition(), true);
		}
		//body.applyForceToCenter(0, 40, true);
	}
	
	public void rotateAimLeft() {
		rotateAim(-1);
	}
	public void rotateAimRight() {
		rotateAim(1);
	}
	private void rotateAim(int dir) {
		didRotateLastFrame = true;
		sightRotationAmount -= currentRotationSpeed * dir * dt / 0.016666f;
		rotatingBody.setTransform(rotatingBody.getPosition(), sightRotationAmount);
		if (currentRotationSpeed < 0.15f)
			currentRotationSpeed += 0.011 * dt / 0.0166666f;
	}
	public void notRotating() {
		if (didRotateLastFrame) {
			didRotateLastFrame = false;
			currentRotationSpeed = rotationSpeed;
		}
	}
	
	public void fire() {
		ballsTryingToBeFired += (dt / firingRate);
		int ballsToFireThisFrame = (int) (ballsTryingToBeFired) ;
		for (int i = 1; i <= ballsToFireThisFrame; i++) {
			bdefFire.position.set(sightBody.getPosition());
			firedBalls.add(new FiredBall());
			ballsTryingToBeFired--; // shot a ball
		}		
	}
	private void updateFiredBalls() {
		for (int i = 0; i < firedBalls.size; i++) {
			firedBalls.get(i).update();
			if (firedBalls.get(i).isDead) firedBalls.removeIndex(i);
		}
	}
	
	private class FiredBall {
		private Body fireBody;
		private float deathCountdown = 1.0f; // how long they last in seconds
		public boolean isDead = false;
		public FiredBall() {
			fireBody = world.createBody(bdefFire);
			fireBody.createFixture(fdefFire).setUserData("fire");
			Vector2 fireVector = vectorComponent(
					body.getPosition().x, body.getPosition().y,
					sightBody.getPosition().x, sightBody.getPosition().y, 
					8f);
			fireBody.applyLinearImpulse(fireVector, fireBody.getPosition(), true);
			// there's a reason that we didn't add recoil :(
			//body.applyLinearImpulse(-fireVector.x, -fireVector.y, body.getPosition().x, body.getPosition().y, true);
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
	

	public void input(int up, int down, int left, int right, int rotateLeft, int rotateRight, int fire) {
	//public void input(int up, int down, int left, int right, int modifier) {
		/*if (Gdx.input.isKeyPressed(modifier)) {
			if (Gdx.input.isKeyPressed(left)) rotateAimLeft();
			else if (Gdx.input.isKeyPressed(right)) rotateAimRight();
			else notRotating();
			if (Gdx.input.isKeyPressed(up)) fire();
		}
		else {
			if (Gdx.input.isKeyPressed(left)) moveLeft();
			if (Gdx.input.isKeyPressed(right)) moveRight();
			if (Gdx.input.isKeyJustPressed(up)) jump();
		}/**/
		if (Gdx.input.isKeyPressed(left)) moveLeft();
		if (Gdx.input.isKeyPressed(right)) moveRight();
		if (Gdx.input.isKeyJustPressed(up)) jump();
		if (Gdx.input.isKeyPressed(rotateLeft)) rotateAimLeft();
		else if (Gdx.input.isKeyPressed(rotateRight)) rotateAimRight();
		else notRotating();
		if (Gdx.input.isKeyPressed(fire)) fire();/**/
	}
	
}
