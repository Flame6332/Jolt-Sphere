package org.joltsphere.mechanics;

import org.joltsphere.main.JoltSphereMain;

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
import com.badlogic.gdx.utils.Array;

public class ArenaPlayer {
	
	
	public float ppm = JoltSphereMain.ppm;
	public float FPSdt = 1f / JoltSphereMain.FPS;
	
	public Body body;
	public Fixture fixture;
	public CircleShape circShape;
	public CircleShape jumpShape;
	public PolygonShape smashShape;
	public World world;
	public Vector2 locationIndicator;
	public Vector2 startingLocation;
	public Color color;
	public Array<Vector2> trail;
	public Array<Vector2> paint;
	
	public FixtureDef fdefBall;
	public FixtureDef fdefSmash;
	public FixtureDef fdefSmashJump;
	
	public int knockouts = 0;
	public int player;
	private float dt;
	
	public boolean isSmashing = false;
	public boolean isSmashJumping = false;
	public boolean isMagnifying = false;
	
	public boolean hasDoubled = true;
	public boolean canJump = false;
	public boolean canHold = false;
	public boolean canAttack = false;
	public boolean canSmashJump = false;
	public boolean hadPreviouslySmashedLastFrame = false;
	private boolean hadPreviouslyMagnifiedLastFrame = false;
	public boolean shouldLocationIndicate = false;
	public boolean isGrounded = false;
	public boolean wasKnockedOut = false;
	
	public float smashRestitution = 0; // restitution of smash object
	public float smashDensity = 80f;
	public float smashJumpRestitution = 0.6f; 
	public float smashJumpDensity = 1500f;
	
	public float energyTimerSpeed = 1/50f;
	public float energyTimer = 1;
	public float minimumEnergy = 0.05f;
	
	public boolean wasHitBySmash = false;
	public float currentRecievingSmashRestitution = 0; // restitution whenever player hit with a smash
	public float recievingSmashRestitution = 0.4f;
	public float beforeContactRestitution = 0;
	public float maximumContactRestitution = 1 / minimumEnergy * recievingSmashRestitution;
	
	public int jumpDelay = 7;
	public float jumpTimer = jumpDelay;
	
	public float jumpHoldPhase = 15; //half jump time
	public float jumpHoldTimer = jumpHoldPhase;
	
	public float smashLength = 60;
	public float smashTimer = smashLength;
	
	private float magnifyLength = 60;
	private float magnifyTimer = magnifyLength;
	
	public float attackCooldownLength = 250;
	public float attackCooldown = 0;

	public float smashJumpLength = 17; //length of jump
	public float smashJumpPeriodLength = 40; //period to jump
	public float smashJumpPeriod = smashJumpPeriodLength;
	
	public float arenaSpace;
	private float indicatorScl = 1; // place holder value of 1
	private float indicatorSclLimit = 0.01f;
	public float indicatorSize = 1f;
	
	public ArenaPlayer (int xpos, int ypos, World world, int player, Color color) {
	
		this.player = player;
		
		this.world = world;

		this.color = color;
		
		circShape = new CircleShape();
		circShape.setRadius(26 / 100f);
		
		jumpShape = new CircleShape();
		jumpShape.setRadius(60 / 100f);
		
		locationIndicator = new Vector2();
		startingLocation = new Vector2(xpos, ypos);
		
		createFixtureDefs();
		
		createBall(xpos, ypos);
		
		trail = new Array<Vector2>();
		paint = new Array<Vector2>();
				
	}
	
	
	public void update(int contact, float deltaTime, int width, int height) {
		dt = deltaTime;
		arenaSpace = 0.5f * height;
		
		/* Basic Values if on the Ground */
		checkIfGrounded(contact);
		
		/* Creates timer to jump while bouncing around */
		updateJumpTimers(dt);
			
		/* Allows for smash jump after end of smash */
		updateSmashJump(dt);
		
		/* Sequence to preform if no longer smashing */
		updateAttackCooldown(dt);
		
		// Updates Indicator
		updateLocationIndicator(width, height);
		// Checks if Dead
		checkIfDead(width, height);
		
		updateEnergy();
		
		//updatePaint();
	}

	
	public void shapeRender(ShapeRenderer sRender) {
		
		sRender.setColor(color);
		//for (int i = 1; i < trail.size; i++) {
			//sRender.circle(trail.get(i).x * ppm, trail.get(i).y * ppm, 50);
		//}
		
		/*if (shouldLocationIndicate) {
			float r = (indicatorSize / 2) *(1/indicatorSclLimit);
			sRender.rect(locationIndicator.x - r, locationIndicator.y - r, r*2, r*2);
			sRender.circle(locationIndicator.x, locationIndicator.y, r);
		}*/
		
		sRender.setColor(Color.GOLD);
			if (!isSmashing && !isSmashJumping) 
				sRender.circle(body.getPosition().x * ppm, body.getPosition().y * ppm, (circShape.getRadius()*100 + 1) * (ppm / 100f));
			
			if (shouldLocationIndicate) {
				//float r = (indicatorSize / 2) *(1/indicatorSclLimit);
				//sRender.rect(locationIndicator.x - r, locationIndicator.y - r, r*2, r*2);
				//sRender.circle(locationIndicator.x, locationIndicator.y, r);
			}
		
		
		sRender.setColor(color);
		if (isSmashing) 
			sRender.circle(body.getPosition().x * ppm, body.getPosition().y * ppm, 15 * (ppm / 100f));
		else if (isSmashJumping) 
			sRender.circle(body.getPosition().x * ppm, body.getPosition().y * ppm, jumpShape.getRadius()*100 * (ppm / 100f));
		else 
			sRender.circle(body.getPosition().x * ppm, body.getPosition().y * ppm, circShape.getRadius()*100 * (ppm / 100f));
		
		sRender.setColor(Color.WHITE);
		if (shouldLocationIndicate) {
			float area = (float) (Math.pow(		(indicatorSize / 2) *(1/indicatorSclLimit)		, 2) * Math.PI);
			float r = (float) (Math.pow((		// sqrt( pi*r^2 * 1/scl  ) 
					(area  * (1.01 - indicatorScl)) 	/ Math.PI),	
					0.5 /* square root */) + 2);	
			sRender.circle(locationIndicator.x, locationIndicator.y, r);
			//sRender.rect(locationIndicator.x - r, locationIndicator.y - r, r*2, r*2);
		}
		
	}
	
	public void renderPaint(ShapeRenderer shapeRender, int i) {
		shapeRender.setColor(color);
		shapeRender.circle(paint.get(i).x * ppm, paint.get(i).y * ppm, 50);
	}
	
	@SuppressWarnings("unused")  
	private void updatePaint() {
		paint.add(new Vector2(body.getPosition()));
		if (paint.size >= 300) paint.removeIndex(0);
	}
	
	private void checkIfGrounded(int contact) {
		if (contact > 0) {//if on ground
			hasDoubled = false; //reset double jump
			canJump = true;
			jumpTimer = jumpDelay;
			isGrounded = true;
		} 
		else isGrounded = false;
	}
	
	public Vector2 getPosition() {
		return new Vector2(body.getPosition().x * ppm, body.getPosition().y * ppm);
	}
	
	public void moveLeft (float percent) {
		moveHorizontal(-1, percent);
	}
	public void moveRight (float percent) {
		moveHorizontal(1, percent);
	} 
	
	private void moveHorizontal (int dir, float percent) {
		if (isSmashing) { // smashing
			if (canJump) { 
				body.applyForceToCenter(50000 * dir * 0.01666666f, 0, true);
				body.applyTorque(-100f * dir, true);
			} else { // air smashing
				body.applyForceToCenter(60000 * dir * 0.01666666f, 0, true);
				body.applyTorque(-100f * dir, true);
			}
		}
		else { // not smashing
			if (isGrounded) {
				body.applyTorque(-12f * dir, true);
				body.applyForceToCenter(1200 * dir * 0.01666666f, 0, true);
			} else {
				body.applyForceToCenter(900 * dir * 0.01666666f, 0, true);
			}
		}	
	}
	
	
	
	public void jump () { //no delta because single impulse
		if (canJump) { //if on ground
			if (canSmashJump) smashJump();
			else { 
				body.setLinearVelocity(body.getLinearVelocity().x * 0.3f, body.getLinearVelocity().y * 0.3f);
				body.applyLinearImpulse(new Vector2(0, 280f * 0.016666666f), body.getPosition(), true);
				jumpHoldTimer = jumpHoldPhase;
			}
		}
		else if (!hasDoubled) {
			body.setAngularVelocity(0);
			body.setLinearVelocity(0, 0);
			body.applyLinearImpulse(new Vector2(0, 310f * 0.01666666f), body.getPosition(), true);
			hasDoubled = true;
		}
	}
	public void jumpHold () {
		if (!hasDoubled && canHold && !isSmashing) {
			body.applyForceToCenter(0, 900 * 0.01666666f, true);
		}
	}
	
	private void updateJumpTimers(float dt) {
		if (jumpTimer > 0) {
			canJump = true;
			
			
			jumpTimer -= 60 * dt; 
		}
		else canJump = false;
			//similar, except timer for held jumps
			if (jumpHoldTimer > 0) {
				canHold = true;
				jumpHoldTimer -= 60 * dt;
			} 
			else canHold = false;
	}
	
	private void smashJump() {
		hasDoubled = true; // so you cant double jump during smash jummp
		isSmashJumping = true;
		
		body.destroyFixture(fixture);
		fixture = body.createFixture(fdefSmashJump);
		fixture.setUserData("p" + player);
		fixture.setRestitution(fixture.getRestitution() + (currentRecievingSmashRestitution / maximumContactRestitution));
		
		smashJumpPeriod = smashJumpLength;
		body.setAngularVelocity(body.getAngularVelocity() * 0.3f); // rotational speed decreased 30%
		body.setLinearVelocity(body.getLinearVelocity().x * 0.3f, body.getLinearVelocity().y * 0.1f); // velocities decreased 30% and 10%
		body.applyLinearImpulse(new Vector2(0, 2000000f * 0.0166666f), body.getPosition(), true);
	}
	private void updateSmashJump(float dt) {
		if (smashJumpPeriod > 0) { // if you can still smash jump
			canSmashJump = true;
			smashJumpPeriod -= 60 * dt;
		}
		else if (canSmashJump){ // if youre out of time but the variable still there
			attackCooldown = 0;
			canSmashJump = false;
			isSmashJumping = false;
			body.setLinearVelocity(body.getLinearVelocity().x * 0.5f, body.getLinearVelocity().y * 0.05f); //not absolute stop
			smashEnded(); // called in smash jump because it technically a smash by shape
		}
	}
	
	
	public boolean isAttacking() {
		if (isSmashing || isMagnifying || isSmashJumping) {
			return true;
		} else return false;
	}
	private void updateAttackCooldown(float dt) {
		if (!isAttacking()) {
			if (attackCooldown > attackCooldownLength) canAttack = true;
			else attackCooldown += 60 * dt;
		}
	}
	
	public void smash() {
		if (canAttack) {
			if (smashTimer == smashLength) smashBegin(); // timer has not been changed yet, so begin smash
			if (!isGrounded) body.applyForceToCenter(0, -30000 * 0.01666666f, true);
			if (canJump) body.applyForceToCenter(0, -30000 * 0.0166666f, true);
			isSmashing = true;   
			hadPreviouslySmashedLastFrame = true;
			smashTimer-=60*dt;
			if (smashTimer < 0) smashEnded();
		}
	}
	public void notSmashing() { // called in keyboard controls when finger released
		if (hadPreviouslySmashedLastFrame) { // called if you smashed last frame
			smashJumpPeriod = smashJumpPeriodLength; // gives you a smash jump period since you released before smash end
			hadPreviouslySmashedLastFrame = false; // updates this boolean
			if (canJump) canSmashJump = true; // allows you to smash jump during smash
			smashEnded(); // ends smash
		}
	}
	private void smashBegin() {
		body.destroyFixture(fixture);
		fixture = body.createFixture(fdefSmash);
		fixture.setUserData("p" + player);
	}
	private void smashEnded() {
		isSmashing = false;
		smashTimer = smashLength;
		canAttack = false;
		attackCooldown = 0;
		body.destroyFixture(fixture);
		fixture = body.createFixture(fdefBall);
		fixture.setUserData("p" + player);
		beforeContactRestitution = fixture.getRestitution();
	}
	
	public void hitBySmash() { //called when get smashed
		wasHitBySmash = true;
	}
	public void notHitBySmash(ArenaPlayer otherPlayer) { // called whenever there isnt any contact with player in the update cycle
		if (wasHitBySmash) {
			wasHitBySmash = false;
			float scale = 500f / maximumContactRestitution * currentRecievingSmashRestitution;
			body.applyLinearImpulse(new Vector2(otherPlayer.body.getLinearVelocity().x * scale * 0.01666666f, otherPlayer.body.getLinearVelocity().y * scale * 0.01666666f), body.getPosition(), true);
		}
	}
	
	public void magnify(Vector2 otherPlayer) {
		if (canAttack) {
			body.applyForce(vectorComponent(
					body.getPosition().x, body.getPosition().y, 
					otherPlayer.x, otherPlayer.y, 
					100), new Vector2(0,0), true);
			isMagnifying = true;
			attackCooldown = 0;
			magnifyTimer -= 60*dt;
			if (magnifyTimer < 0) magnifyEnded();
			hadPreviouslyMagnifiedLastFrame = true;
		}
	}
	public void notMagnifying() {
		if (hadPreviouslyMagnifiedLastFrame) magnifyEnded();
	}
	private void magnifyEnded() {
		isMagnifying = false;
		hadPreviouslyMagnifiedLastFrame = false;
		magnifyTimer = magnifyLength;
		canAttack = false;
	}
	public boolean canMagnify() {
		if (canAttack) return true;
		else return false;
	}
 	public void otherPlayerMagnified(Vector2 otherPlayer) {
 		body.applyForce(vectorComponent(
				body.getPosition().x, body.getPosition().y, 
				otherPlayer.x, otherPlayer.y, 
				100), new Vector2(0,0), true);/**/
	}
	
	public void knockedOut() {
		knockouts++;
		
		body.setAngularVelocity(0);
		body.setLinearVelocity(0, 0);
		body.setTransform(startingLocation.x / ppm, startingLocation.y / ppm, 0);
		
		wasKnockedOut = true; 
		
		resetEnergy();
	}
	
	public void resetEnergy() {
		createFixtureDefs();
		energyTimer = 1;
	}
	
	public void otherPlayerKnockedOut() {
		resetEnergy();
	}
	
	void updateEnergy() {
		if (energyTimer > minimumEnergy + energyTimerSpeed*dt) // add energy timer speed for going below minimum 
			energyTimer -= energyTimerSpeed * dt; // lower energy timer
		currentRecievingSmashRestitution = 1f/energyTimer * recievingSmashRestitution; 
	}
	
	public void contactingOtherPlayer(ArenaPlayer otherPlayer) {
		if (otherPlayer.isSmashing) hitBySmash();
	}
	public void notContactingOtherPlayer(ArenaPlayer otherPlayer) {
		notHitBySmash(otherPlayer);
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
				/*if (y < hMid) indicatorScl = 1 - ((-1*y) * shrnk); // below
				else indicatorScl = 1 - ((y - h) * shrnk); // above	*/
				
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
		fdefBall.density = 5;//(5 / 0.01666666f) * FPSdt;        
		
		fdefSmash = new FixtureDef();
		fdefSmash.shape = createSmashShape(1/1.2f);
		fdefSmash.friction = 0.2f;
		fdefSmash.restitution = smashRestitution;
		fdefSmash.density = smashDensity;//(80 / 0.01666666f) * FPSdt;        
		
		fdefSmashJump = new FixtureDef();
		fdefSmashJump.shape = jumpShape;//createSmashShape(2f);
		fdefSmashJump.friction = 0.5f;
		fdefSmashJump.restitution = smashJumpRestitution;
		fdefSmashJump.density = smashJumpDensity;//(1500 / 0.01666666f) * FPSdt;
		
	}
	
	private PolygonShape createSmashShape(float scl) {
		
		PolygonShape shape = new PolygonShape();
		
		Vector2[] v = new Vector2[8];
		//bottom left
		v[0] = new Vector2(-30 * scl / 100, -12.5f * scl / 100);
		v[1] = new Vector2(-12.5f * scl / 100, -30 * scl / 100);
		
		//bottom right
		v[2] = new Vector2(12.5f * scl / 100, -30 * scl / 100);
		v[3] = new Vector2(30 * scl / 100, -12.5f * scl / 100);
		
		//top right
		v[4] = new Vector2(30 * scl / 100, 12.5f * scl / 100);
		v[5] = new Vector2(12.5f * scl / 100, 30 * scl / 100);
		
		//top left
		v[6] = new Vector2(-12.5f * scl / 100, 30 * scl / 100);
		v[7] = new Vector2(-30 * scl / 100, 12.5f * scl / 100);
	
		shape.set(v);
		
		return shape;
		
	}
	
}
