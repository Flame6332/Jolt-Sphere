package org.joltsphere.mechanics;

import org.joltsphere.main.JoltSphereMain;
import org.joltsphere.misc.ObjectData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class MountainClimbingPlayer {
	
	@SuppressWarnings("unused")
	private World world;
	public Body body;
	private BodyDef bdef;
	private FixtureDef fdef;
	private CircleShape circle;
	
	private Color color;
	private boolean isGrounded = false;
	private boolean canDouble = false;
	
	private float ppm = JoltSphereMain.ppm;
	@SuppressWarnings("unused")
	private float dt = 0.01666f;
	
	public MountainClimbingPlayer(World world, float x, float y, Color color) {
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
		body.setUserData(new ObjectData("mountainClimber"));
		fdef = new FixtureDef();
		circle = new CircleShape();
		circle.setRadius(50 / ppm);
		fdef.shape = circle;
		fdef.friction = 0.1f;
		fdef.density = 10;
		fdef.restitution = 0f;
		body.createFixture(fdef);
		
	}
	
	public void shapeRender(ShapeRenderer shapeRender) {
		shapeRender.setColor(color);
		shapeRender.circle(body.getPosition().x*ppm,body.getPosition().y*ppm, 50);
		shapeRender.setColor(Color.GOLD);
	}
	
	public void update(float dt, int groundContacts) {
		this.dt = dt;
		
		if (groundContacts > 0) isGrounded = true; 
		else isGrounded = false;
		
		if (isGrounded) {
			canDouble = true;
		}
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
		if (canDouble) {
			canDouble = false;
			body.setLinearVelocity(0, 0);
			body.applyLinearImpulse(new Vector2(0, 12), body.getPosition(), true);
		}
		//body.applyForceToCenter(0, 40, true);
	}
	public void fly() {
		body.applyForceToCenter(new Vector2(0, 60), true);
	}

	public void input(int up, int down, int left, int right, int modifier, boolean fly) {	
		if (Gdx.input.isKeyPressed(left)) moveLeft();
		if (Gdx.input.isKeyPressed(right)) moveRight();
		if (Gdx.input.isKeyPressed(up) && fly) fly();
		else if (Gdx.input.isKeyJustPressed(up)) jump();
	}
	
}
