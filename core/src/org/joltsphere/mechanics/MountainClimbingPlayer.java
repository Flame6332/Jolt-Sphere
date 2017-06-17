package org.joltsphere.mechanics;

import org.joltsphere.main.JoltSphereMain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class MountainClimbingPlayer {
	
	@SuppressWarnings("unused")
	private World world;
	public Body body;
	private Fixture fixture;
	private BodyDef bdef;
	private FixtureDef fdef;
	private CircleShape circle;
	
	private Color color;
	private boolean isGrounded = false;
	
	private float ppm = JoltSphereMain.ppm;
	@SuppressWarnings("unused")
	private float dt = 0.01666f;
	
	public MountainClimbingPlayer(World world, int x, int y, Color color) {
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
		fdef.density = 10;
		fdef.restitution = 0f;
		fdef.filter.categoryBits = 1;
		fdef.filter.maskBits = 1;
		fixture = body.createFixture(fdef);
		fixture.setUserData("mountainClimber");
		
				
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
			body.applyLinearImpulse(0, 16, 0, 0, true);
		}
		//body.applyForceToCenter(0, 40, true);
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
	

	public void input(int up, int down, int left, int right, int modifier) {	
		if (Gdx.input.isKeyPressed(left)) moveLeft();
		if (Gdx.input.isKeyPressed(right)) moveRight();
		if (Gdx.input.isKeyJustPressed(up)) jump();
	}
	
}
