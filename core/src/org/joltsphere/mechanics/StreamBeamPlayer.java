package org.joltsphere.mechanics;

import org.joltsphere.main.JoltSphereMain;
import org.joltsphere.misc.Misc;
import org.joltsphere.misc.ObjectData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class StreamBeamPlayer {
	
	private World world;
	public Body body;
	private Body sightBody;
	private Body rotatingBody;
	private Array<FiredBall> firedBalls;
	private BodyDef bdef;
	private BodyDef bdefFire;
	private FixtureDef fdef;
	private FixtureDef fdefFire;
	private CircleShape circle;
	private CircleShape sightCircle;
	
	private Color color;
	public float density() {return 10f;};
	
	private boolean isGrounded = false;
	private boolean canDoubleJump = false;
	
	private float firingRate = 0.016667f; // the smaller the faster
	private float ballsTryingToBeFired = 0;
	private int firedBallCount = 0;
	public Array<Integer> firedBallsToBeRemoved;
	private boolean canFire = true;
	private float energyLevel = 0;
	private float maxEnergy = 100; 
	
	private float sightRotationAmount = 0;
	private boolean didRotateLastFrame = false;
	private float rotationSpeed = 0.005f; // beginning rotation speed
	private float currentRotationSpeed = rotationSpeed;
	
	private float ppm = JoltSphereMain.ppm;
	private float dt = 0.01666f;
	
	public StreamBeamPlayer(World world, float x, float y, Color color) {
		this.world = world;
		this.color = color;
		
		createObjects(x, y);
		firedBallsToBeRemoved = new Array<Integer>();
			
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
		
		shapeRender.setColor(Color.WHITE);
		shapeRender.rect(0, body.getPosition().y*ppm, maxEnergy*5+30, 100);
		
		shapeRender.setColor(Color.YELLOW);
		shapeRender.rect(30, body.getPosition().y*ppm+15, energyLevel*5, 70);
	}
	
	public void update(float dt, int groundContacts) {
		this.dt = dt;
		updateFiredBalls();
		
		if (groundContacts > 0) isGrounded = true;
		else isGrounded = false;
		
		if (isGrounded) canDoubleJump = true;
		
		if (energyLevel > 0) energyLevel -= 0.125f;
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
		else if (canDoubleJump) {
			canDoubleJump = false;
			body.setLinearVelocity(0, 0);
			body.applyLinearImpulse(new Vector2(0, 12), body.getPosition(), true);
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
		if (energyLevel >= maxEnergy) canFire = false;
		else canFire = true;
		if (canFire) {
			energyLevel += 1;
			ballsTryingToBeFired += (dt / firingRate);
			int ballsToFireThisFrame = (int) (ballsTryingToBeFired) ;
			for (int i = 1; i <= ballsToFireThisFrame; i++) {
				bdefFire.position.set(sightBody.getPosition());
				firedBalls.add(new FiredBall());
				ballsTryingToBeFired--; // shot a ball
			}
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
		private int count;
		public FiredBall() {
			fireBody = world.createBody(bdefFire);
			firedBallCount++;
			count = firedBallCount;
			fireBody.setUserData(new ObjectData(firedBallCount, "fire"));
			fireBody.createFixture(fdefFire);
			Vector2 fireVector = 
					Misc.vectorComponent(body.getPosition(), sightBody.getPosition(), 8f);

			fireBody.applyLinearImpulse(fireVector, fireBody.getPosition(), true);
			// there's a reason that we didn't add recoil :(
			//body.applyLinearImpulse(-fireVector.x, -fireVector.y, body.getPosition().x, body.getPosition().y, true);
			if (density() > 1) body.applyLinearImpulse(-fireVector.x *0.07f, -fireVector.y *0.07f, body.getPosition().x, body.getPosition().y, true);
		}
		public void update() {
			boolean shouldDestroy = false;
			for (Integer i : firedBallsToBeRemoved) {
				if (i.intValue() == count) shouldDestroy = true;
				//System.out.println(i + " " + ((ObjectData)fireBody.getUserData()).count);
			}
			if (deathCountdown < 0 || shouldDestroy) {
				firedBallsToBeRemoved.removeValue(count, true);
				world.destroyBody(fireBody);
				isDead = true;
			}
			/*if (shouldDestroy) {
				firedBallsToBeRemoved.removeValue(count, true);
				if (fireBody.getFixtureList().size > 0) fireBody.getFixtureList().first().setSensor(true);
			}*/
			deathCountdown -= dt;
		}
	}

	public void input(int up, int down, int left, int right, int rotateLeft, int rotateRight, int fire) {
		if (Gdx.input.isKeyPressed(left)) moveLeft();
		if (Gdx.input.isKeyPressed(right)) moveRight();
		if (Gdx.input.isKeyJustPressed(up)) jump();
		if (Gdx.input.isKeyPressed(rotateLeft)) rotateAimLeft();
		else if (Gdx.input.isKeyPressed(rotateRight)) rotateAimRight();
		else notRotating();
		if (Gdx.input.isKeyPressed(fire)) fire();
	}
	
	private void createObjects(float x, float y) {

		bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(x / ppm, y / ppm);
		bdef.fixedRotation = false;
		bdef.bullet = true;
		bdef.linearDamping = 0.2f;
		bdef.angularDamping = 0.5f;
		body = world.createBody(bdef);
		body.setUserData(new ObjectData("streamBeam"));
		fdef = new FixtureDef();
		circle = new CircleShape();
		circle.setRadius(50 / ppm);
		fdef.shape = circle;
		fdef.friction = 0.1f;
		fdef.density = density();
		fdef.restitution = 0;
		fdef.filter.categoryBits = 1;
		fdef.filter.maskBits = 1;
		body.createFixture(fdef);
		
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
		fdef.density = 0.01f;
		fdef.restitution = 0f;
		fdef.filter.categoryBits = 2;
		fdef.filter.maskBits = 1;
		fdef.shape = circle;
		rotatingBody.createFixture(fdef);
		fdef.shape = sightCircle;
		sightBody.createFixture(fdef);
		
		RevoluteJointDef wdef = new RevoluteJointDef();
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
	
}
