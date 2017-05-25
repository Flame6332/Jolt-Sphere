package org.joltshpere.testing.mechanics;

import org.joltshpere.testing.main.JoltSphereTesting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class TestPlayer {
	
	
	public float ppm = JoltSphereTesting.ppm;
	public float FPSdv = 1f / JoltSphereTesting.FPS;
	
	public Body body;
	public Fixture fixture;
	public CircleShape circShape;
	public CircleShape jumpShape;
	public PolygonShape smashShape;
	public World world;
	public Vector2 locationIndicator;
	public Vector2 startingLocation;
	
	public FixtureDef fdefBall;
	public FixtureDef fdefSmash;
	public FixtureDef fdefSmashJump;
	
	public int knockouts = 0;
	public int player;
	private float dv;
	
	public boolean isSmashing = false;
	public boolean isSmashJumping = false;
	public boolean hasDoubled = true;
	public boolean canJump = false;
	public boolean canHold = false;
	public boolean canSmash = false;
	public boolean canSmashJump = false;
	public boolean previousSmash = false;
	public boolean shouldLocationIndicate = false;
	public boolean isGrounded = false;
	
	public int jumpDelay = 5;
	public float jumpTimer = jumpDelay;
	
	public float jumpHoldPhase = 15; //half jump time
	public float jumpHoldTimer = jumpHoldPhase;
	
	public float smashLength = 60;
	public float smashTimer = smashLength;
	
	public float smashCooldownLength = 250;
	public float smashCooldown = smashCooldownLength;

	public float energyTimerSpeed = 0.1f;
	public float energyTimer = 0;
	
	public float smashJumpLength = 17; //length of jump
	public float smashJumpPeriodLength = 40; //period to jump
	public float smashJumpPeriod = smashJumpPeriodLength;
	
	public float arenaSpace;
	private float indicatorScl = 1; // place holder value of 1
	private float indicatorSclLimit = 0.01f;
	public float indicatorSize = 1f;
	
	public TestPlayer (int xpos, int ypos, World realWorld, int playah) {
	
		player = playah;
		
		world = realWorld;
		
		circShape = new CircleShape();
		circShape.setRadius(26 / 100f);
		
		jumpShape = new CircleShape();
		jumpShape.setRadius(60 / 100f);
		
		locationIndicator = new Vector2();
		startingLocation = new Vector2(xpos, ypos);
		
		createFixtureDefs();
		
		createBall(xpos, ypos);
				
	}
	
	
	public void update(int contact, float delta, int width, int height) {
		dv = delta;
		arenaSpace = 0.5f * height;
		
		/* Basic Values if on the Ground */
		
		if (contact > 0) {//if on ground
			hasDoubled = false; //reset double jump
			canJump = true;
			jumpTimer = jumpDelay;
			isGrounded = true;
		} 
		else isGrounded = false;
		
		/* Creates timer to jump while bouncing around */
		
		if (jumpTimer > 0) {
			canJump = true;
			jumpTimer -= 60 * dv; 
		}
		else canJump = false;
			//similar, except timer for held jumps
			if (jumpHoldTimer > 0) {
				canHold = true;
				jumpHoldTimer -= 60 * dv;
			} 
			else canHold = false;
			
		/* Allows for smash jump after end of smash */
		
		if (smashJumpPeriod > 0) {
			canSmashJump = true;
			smashJumpPeriod -= 60 * dv;
		}
		else if (canSmashJump){
			smashCooldown=0;
			canSmashJump = false;
			isSmashJumping = false;
			body.setLinearVelocity(body.getLinearVelocity().x * 0.5f, body.getLinearVelocity().y * 0.05f); //not absolute stop
		}
		
		/* Sequence to preform if no longer smashing */
		
		if (!isSmashing) {
			if (smashCooldown == 0) {
				smashTimer = smashLength;
				canSmash = false;
				smashCooldown = 1;
				body.destroyFixture(fixture);
				fixture = body.createFixture(fdefBall);
				fixture.setUserData("p" + player);
			}
			else {
				if (smashCooldown > smashCooldownLength) canSmash = true;
				else smashCooldown += 60 * dv;
			}			
		}
		
		// Updates Indicator
		updateLocationIndicator(width, height);
		// Checks if Dead
		checkIfDead(width, height);
		
		if (!isSmashing && !isSmashJumping) weakenPlayer();
		
	}

	
	public void shapeRender(ShapeRenderer sRender, Color skinColor) {
		
		sRender.setColor(skinColor);
		/*if (shouldLocationIndicate) {
			float r = (indicatorSize / 2) *(1/indicatorSclLimit);
			sRender.rect(locationIndicator.x - r, locationIndicator.y - r, r*2, r*2);
			sRender.circle(locationIndicator.x, locationIndicator.y, r);
		}*/
		
		sRender.setColor(Color.GOLD);
			if (!isSmashing && !isSmashJumping) 
				sRender.circle(body.getPosition().x * ppm, body.getPosition().y * ppm, (circShape.getRadius()*100 + 1) * (ppm / 100f));
			
			if (shouldLocationIndicate) {
				float r = (indicatorSize / 2) *(1/indicatorSclLimit);
				//sRender.rect(locationIndicator.x - r, locationIndicator.y - r, r*2, r*2);
				sRender.circle(locationIndicator.x, locationIndicator.y, r);
			}
		
		
		sRender.setColor(skinColor);
		if (isSmashing) 
			sRender.circle(body.getPosition().x * ppm, body.getPosition().y * ppm, 15 * (ppm / 100f));
		else if (isSmashJumping) 
			sRender.circle(body.getPosition().x * ppm, body.getPosition().y * ppm, jumpShape.getRadius()*100 * (ppm / 100f));
		else 
			sRender.circle(body.getPosition().x * ppm, body.getPosition().y * ppm, circShape.getRadius()*100 * (ppm / 100f));
		
		if (shouldLocationIndicate) {
			float area = (float) (Math.pow(		(indicatorSize / 2) *(1/indicatorSclLimit)		, 2) * Math.PI);
			float r = (float) (Math.pow((		// sqrt( pi*r^2 * 1/scl  ) 
					(area  * (1.01 - indicatorScl)) 	/ Math.PI),	
					0.5 /* square root */) + 2);	
			sRender.circle(locationIndicator.x, locationIndicator.y, r);
			//sRender.rect(locationIndicator.x - r, locationIndicator.y - r, r*2, r*2);
		}
		
	}
	
	public void moveLeft () {
		moveHorizontal(-1);
	}
	public void moveRight () {
		moveHorizontal(1);
	} 
	
	private void moveHorizontal (int dir) {	
		if (isSmashing) {
			if (canJump) {
				body.applyForceToCenter(500000 * dir * dv, 0, true);
				body.applyAngularImpulse(-500f * dir * dv, true);
			} else {
				body.applyForceToCenter(60000 * dir * dv, 0, true);
				body.applyForceToCenter(-500f * dir * dv, 0, true);
			}
		}
		else {
			if (isGrounded) {
				body.applyAngularImpulse(-12f * dir * dv, true);
				body.applyForceToCenter((fdefBall.density / 5f) * 1200 * dir * dv, 0, true);
			} else {
				body.applyForceToCenter((fdefBall.density / 5f) * 900 * dir * dv, 0, true);
			}
		}	
	}
	
	
	
	public void jump () { //no delta because single impulse
		if (canJump) { //if on ground
			if (canSmashJump) smashJump();
			else { 
				body.setLinearVelocity(body.getLinearVelocity().x * 0.3f, body.getLinearVelocity().y * 0.3f);
				body.applyForceToCenter(0, (fdefBall.density / 5f) * 280, true);
				//body.applyForceToCenter(0, 500, true);
				jumpHoldTimer = jumpHoldPhase;
			}
		}
		else if (!hasDoubled) {
			body.setAngularVelocity(0);
			body.setLinearVelocity(0, 0);
			body.applyForceToCenter(0, (fdefBall.density / 5f) * 310, true);
			hasDoubled = true;
		}
	}
	public void jumpHold () {
		if (!hasDoubled && canHold && !isSmashing) {
			body.applyForceToCenter(0, (fdefBall.density / 5f) * 900 * dv, true);
		}
	}
	
	private void smashJump() {
		isSmashJumping = true;
		
		body.destroyFixture(fixture);
		fixture = body.createFixture(fdefSmashJump);
		fixture.setUserData("p" + player);
		
		smashJumpPeriod = smashJumpLength;
		body.setAngularVelocity(body.getAngularVelocity() * 0.3f);
		body.setLinearVelocity(body.getLinearVelocity().x * 0.3f, body.getLinearVelocity().y * 0.1f);
		body.applyForceToCenter(0, 2000000, true);
		
	}
	
	
	public void smash() {
		if (canSmash) {
			if (smashTimer == smashLength) {
				body.destroyFixture(fixture);
				fixture = body.createFixture(fdefSmash);
				fixture.setUserData("p" + player);
			}
			if (!isGrounded) body.applyForceToCenter(0, -30000 * dv, true);
			isSmashing = true;
			if (canJump) canSmashJump = true;
			previousSmash = true;
			smashTimer-=60*dv;
			if (smashTimer < 0) canSmash = false;
				smashCooldown = 0; //resetting cooldown timer
				
		}
		else isSmashing = false;
	}
	public void notSmashing() { 
		if (previousSmash) {
			isSmashing = false; 
			smashJumpPeriod = smashJumpPeriodLength;
			previousSmash = false;
		}
		else isSmashing = false;  
	}
	
	
	public void knockedOut() {
		knockouts++;
		
		body.setAngularVelocity(0);
		body.setLinearVelocity(0, 0);
		body.setTransform(startingLocation.x / ppm, startingLocation.y / ppm, 0);
		
		createFixtureDefs();
		energyTimer = 0;
	}
	
	void weakenPlayer() {
		body.destroyFixture(fixture);
		
		if (fdefBall.density > 0.1f) fdefBall.density = 5 + energyTimer;
		
		fixture = body.createFixture(fdefBall);
		fixture.setUserData("p" + player);
		energyTimer -= energyTimerSpeed * dv;
	}
	
	void updateLocationIndicator(int width, int height) {
		int w = width;
		int wMid = width / 2;
		int h = height;
		int hMid = height / 2;
		
		float x = body.getPosition().x * ppm;
		float y = body.getPosition().y * ppm;
		
		float xFin = 0;
		float yFin = 0;
		
		float cls = 0.02f; //close
		float far = 1 - cls;
		
		boolean xin = x < 0 || x > w ? false : true; 
		boolean yin = y < 0 || y > h ? false : true;
		
		if (xin && yin) shouldLocationIndicate = false;
		else { // set position of square
			shouldLocationIndicate = true;
			
			if (!xin && yin) { // ball off edge
				yFin = y;
				if (x < wMid) xFin = w * cls;
				else xFin = w * far;
			}
			else if (xin && !yin) { // ball above or below
				xFin = x;
				if (y < hMid) yFin = w * cls;
				else yFin = h - (w * cls);
			}
			else if (!xin && !yin) { // ball in corner
				if (x < wMid) xFin = w * cls; // to left
					else xFin = w * far; // to right
				if (y < hMid) yFin = w * cls; // below
					else yFin = h - (w * cls); // above
			}
		}
		
		if (xFin < w*cls) xFin = w*cls;
		if (xFin > w*far) xFin = w*far;
		if (yFin < w*cls) yFin = w * cls;
		if (yFin > (h  - w*cls)) yFin = h - (w * cls);
		
		locationIndicator.x = xFin;
		locationIndicator.y = yFin;
		
		if (!xin || !yin) { // set scale of square
			float rng = 1 - indicatorSclLimit; //range
			float shrnk = rng / arenaSpace; // shrink
			
			if (!xin && yin) { // ball off edge
				if (x < wMid) indicatorScl = 1 - ((-1*x) * shrnk);
				else indicatorScl = 1 - ((x - w) * shrnk);
			}
			else if (xin && !yin) { // ball above or below
				if (y < hMid) indicatorScl = 1 - ((-1*y) * shrnk);
				else indicatorScl = 1 - ((y - h) * shrnk);
			}
			else if (!xin && !yin) { // ball in corner
				float tempX, tempY;
				if (x < wMid) tempX = 1 - ((-1*x) * shrnk); // to left
					else tempX = 1 - ((x - w) * shrnk); // to right
				if (y < hMid) tempY = 1 - ((-1*y) * shrnk); // below
					else tempY = 1 - ((y - h) * shrnk); // above
				
				if (tempX < tempY) indicatorScl = tempX; else indicatorScl = tempY; 
			}
		
		}
		
	}
	
	void checkIfDead(int width, int height) {
		//int w = width;
		int h = height;
		//float x = body.getPosition().x * ppm;
		float y = body.getPosition().y * ppm;
		
		//if (x < -arenaSpace || x > (w + arenaSpace)) knockedOut();
		if (y < -arenaSpace || y > (h + arenaSpace)) knockedOut();
		
	}
	
	
	void createBall (int xpos, int ypos) {
		
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(xpos / ppm, ypos / ppm);
		bdef.fixedRotation = false;
		bdef.bullet = true;
		
		body = world.createBody(bdef);
		
		fixture = body.createFixture(fdefBall);
		fixture.setUserData("p" + player);
		
	}
	
	void createFixtureDefs() {
		
		fdefBall = new FixtureDef();
		fdefBall.shape = circShape;
		fdefBall.friction = 0.1f;
		fdefBall.restitution = 0;
		fdefBall.density = 5;//(5 / 0.01666666f) * FPSdv;        
		
		fdefSmash = new FixtureDef();
		fdefSmash.shape = createSmashShape(1/1.2f);
		fdefSmash.friction = 0.2f;
		fdefSmash.restitution = 0.4f;
		fdefSmash.density = 80;//(80 / 0.01666666f) * FPSdv;        
		
		fdefSmashJump = new FixtureDef();
		fdefSmashJump.shape = jumpShape;//createSmashShape(2f);
		fdefSmashJump.friction = 0.5f;
		fdefSmashJump.restitution = .6f;
		fdefSmashJump.density = 1500;//(1500 / 0.01666666f) * FPSdv;
		
	}
	
	private PolygonShape createSmashShape(float scl) {
		
		PolygonShape shape = new PolygonShape();
		
		Vector2[] v = new Vector2[8];
		//bottom left
		v[0] = new Vector2(-30 * scl / 100, -15 * scl / 100);
		v[1] = new Vector2(-15 * scl / 100, -30 * scl / 100);
		
		//bottom right
		v[2] = new Vector2(15 * scl / 100, -30 * scl / 100);
		v[3] = new Vector2(30 * scl / 100, -15 * scl / 100);
		
		//top right
		v[4] = new Vector2(30 * scl / 100, 15 * scl / 100);
		v[5] = new Vector2(15 * scl / 100, 30 * scl / 100);
		
		//top left
		v[6] = new Vector2(-15 * scl / 100, 30 * scl / 100);
		v[7] = new Vector2(-30 * scl / 100, 15 * scl / 100);
	
		shape.set(v);
		
		return shape;
		
	}
	
}
