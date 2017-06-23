package org.joltsphere.misc;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class EllipseFixture {

	public EllipseFixture() {
	}
	
	private static float hW, hH;
	
	/**Creates an ellipse, this is a convenience method that creates a fixture defintion based off of the paramaters.*/
	public static Body createEllipseFixtures(Body body, float density, float restitution, float friction, float halfWidth, float halfHeight, String userData) {
		FixtureDef fdef = new FixtureDef();
		fdef.restitution = restitution;
		fdef.density = density;
		fdef.friction = friction;
		return createEllipseFixtures(body, fdef, halfWidth, halfHeight, userData);
	}
	
	/**Creates four fixtures in the shape of an ellipse based off of these paramaters*/
	public static Body createEllipseFixtures(Body body, FixtureDef fdef, float halfWidth, float halfHeight, String userData) {
		// if the ellipse is long and skinny, then for approximation, the ellipse is rotated then rotated back
		boolean shouldRotate = false;  
		if (halfWidth > halfHeight) {
			hW = halfWidth; hH = halfHeight; 
		}
		else {
			hH = halfWidth; hW = halfHeight;
			shouldRotate = true;
		}
		
		PolygonShape poly = new PolygonShape();
		
		float perimeter = (float) (2 * Math.PI * Math.sqrt((Math.pow(hW, 2) + Math.pow(hH, 2)) / 2f)); // perimeter of the ellipse  
		float faceLength = perimeter / 4f / 6f; // splits into 4 quadrants, then into 6 faces
		
		Vector2[] v = new Vector2[8]; // max 8 vertices in a box2d polygon
		
		v[0] = new Vector2(0, 0); // sets first point at center
		v[1] = new Vector2(hW, 0); // set the second point on half the width
		
		float prevX = hW, prevY = 0; // previous point on ellipse in loop
		float x = 0, y = 0;
		
		for (int i = 1; i <= 5; i++) { // for the next 5 points
			
			float dtl = 10f; // detail of calculations
			float iterationSize = faceLength / dtl; // the x axis iteration length, set to face length because it will not exceed the hypotenuse
			float tinyIterationSize = iterationSize / dtl; // once large scale iteration finds an approximated point, the tiny iteration loop does it again to be more precise
			// this algorithim would take the approximation power requirement down from dtl^2 to dtl*2 which is pretty great
			float prevTempX = 0;
			for (int j = 1; j <= dtl; j++) {
				float currentW = iterationSize * j; // gets current width of triangle
				float currentH = ellipseFunction(prevX - currentW)  - prevY; // current height of face triangle
				if (Math.hypot(currentW, currentH) > faceLength) break; // discontinues all code within this for loop
				prevTempX = prevX - currentW; // the coordinates of this iteration, if still looping
			}
			for (int n = 1; n <= dtl; n++) {
				float currentW = prevX - prevTempX + (tinyIterationSize * n); // gets current precise width of the face triangle
				float currentH = ellipseFunction(prevTempX - (tinyIterationSize * n))  - prevY; // current precise height of the face triangle
				if (Math.hypot(currentW, currentH) > faceLength) break; // hypotenuse of imaginary triangle has exceeded facelength, so it can stop now
				x = prevX - currentW; y = ellipseFunction(x); // sets the coordinates of the new face every loop 
			}
			prevX = x; prevY = y; // set the new starting point for the next vertex approximation
			v[i+1] = new Vector2(x, y);
			
		}
		
		v[7] = new Vector2(0, hH);
		
		if (shouldRotate) { // rotates all the vectors back to their appropriate loacation
		for (int i = 0; i <= 7; i++) {
			v[i] = new Vector2(v[i].rotate90(1));
		}}
		
		poly.set(v);
		fdef.shape = poly;
		body.createFixture(fdef).setUserData(userData); // top right fixture
		
		for (int i = 0; i <= 7; i++) {
			v[i] = new Vector2(v[i].x * -1, v[i].y); // flips each vector across the x axis
		}
		poly.set(v);
		fdef.shape = poly;
		body.createFixture(fdef).setUserData(userData); // top left fixture
		
		for (int i = 0; i <= 7; i++) {
			v[i] = new Vector2(v[i].x, v[i].y * -1); // flips the previous fixture across the y axis
		}
		poly.set(v);
		fdef.shape = poly;
		body.createFixture(fdef).setUserData(userData); // bottom left fixture
		
		for (int i = 0; i <= 7; i++) { 
			v[i] = new Vector2(v[i].x * -1, v[i].y); // flips the previous vector across the x axis
		}
		poly.set(v);
		fdef.shape = poly;
		body.createFixture(fdef).setUserData(userData); // bottom right fixture
		poly.dispose();
		
		return body;
	}

	private static float ellipseFunction(float x) {
		return (float) Math.sqrt(Math.pow(hH,2) * (1 - (Math.pow(x,2) / Math.pow(hW, 2)))); // function for half an ellipse
	}
	
}