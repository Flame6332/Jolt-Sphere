package org.joltsphere.misc;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class EllipseFixture {

	public EllipseFixture() {
	}
	
	private static float hW, hH;
	
	public static void createEllipseFixtures(Body body, float halfWidth, float halfHeight, float density, float restitution, float friction) {
		hW = halfWidth; hH = halfHeight; 
		
		FixtureDef fdef = new FixtureDef();
		fdef.density = density;
		fdef.restitution = restitution;
		fdef.friction = friction;
		
		PolygonShape poly = new PolygonShape();
		
		float perimeter = (float) (2 * Math.PI * Math.sqrt((Math.pow(hW, 2) + Math.pow(hH, 2)) / 2f));
		float faceLength = perimeter / 4f / 5f; // splits into 4 quadrants, then into 5 faces
		
		Vector2[] v = new Vector2[8];
		
		v[0] = new Vector2(0, 0);
		v[1] = new Vector2(hW, 0);
		
		float prevX = hW, prevY = 0; // previous point on ellipse in loop
		float x = 0, y = 0;
		
		for (int i = 1; i <= 5; i++) {
			
			int j;
			float iterationSize = faceLength / 30f;
			float tinyIterationSize = iterationSize/30f;
			float prevTempX = 0;
			for (j = 1; j <= 30; j++) {
				float currentW = iterationSize * j; // gets current width of triangle
				float currentH = ellipseFunction(prevX - currentW)  - prevY; // current height of face triangle
				if (Math.hypot(currentW, currentH) > faceLength) break;
				prevTempX = prevX - currentW; // the coordinates of this iteration, if still looping
			}
			for (int n = 1; n <= 30; n++) {
				float currentW = prevX - prevTempX - (tinyIterationSize * n); // gets current precise width of triangle
				float currentH = ellipseFunction(prevTempX - (tinyIterationSize * n))  - prevY; // current precise height of face triangle
				if (Math.hypot(currentW, currentH) > faceLength) break; // found the right size
				x = prevX - currentW; y = ellipseFunction(x); // sets the coordinates of the new face
			}
			
			prevX = x; prevY = y;
			v[i+1] = new Vector2(x, y);
			
		}
		
		v[7] = new Vector2(0, hH);
		
		poly.set(v);
		fdef.shape = poly;
		body.createFixture(fdef);
		
		for (int i = 0; i <= 7; i++) {
			v[i] = new Vector2(v[i].x * -1, v[i].y);
		}
		poly.set(v);
		fdef.shape = poly;
		body.createFixture(fdef);
		
		for (int i = 0; i <= 7; i++) {
			v[i] = new Vector2(v[i].x, v[i].y * -1);
		}
		poly.set(v);
		fdef.shape = poly;
		body.createFixture(fdef);
		
		for (int i = 0; i <= 7; i++) {
			v[i] = new Vector2(v[i].x * -1, v[i].y);
		}
		poly.set(v);
		fdef.shape = poly;
		body.createFixture(fdef);
		
	}

	private static float ellipseFunction(float x) {
		return (float) Math.sqrt(Math.pow(hH,2) * (1 - (Math.pow(x,2) / Math.pow(hW, 2)))); // equation for half an ellipse
	}
	
}
