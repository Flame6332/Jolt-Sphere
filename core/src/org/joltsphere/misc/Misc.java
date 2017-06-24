package org.joltsphere.misc;

import com.badlogic.gdx.math.Vector2;

public class Misc {
	
	/** Just a float version of Math.random() */
	public static float random() {
		return (float) Math.random();
	}
	
	/** Returns a random value between the min and max */
	public static float random(float min, float max) {
		return (max - min) * (float) Math.random() + min;
	}
	
	/** Returns a random value that has been rounded to an integer */
	public static int randomInt(int min, int max) {
		return Math.round(random(min, max));
	}
	
	/** Returns a vector containing the horizontal and vertical components of a vector in the direction of another point
	 * 
	 * @param originX the x position of the vector origin
	 * @param originY the y position of the vector origin 
	 * @param directionX the x position that the vector is directed towards
	 * @param directionY the y position that the vector is directed towards
	 * @param magnitude the magnitude of the given vector
	 */
	public static Vector2 vectorComponent(float originX, float  originY, float  directionX, float directionY, float magnitude) {
		float x1 = originX, y1 = originY, x2 = directionX, y2 = directionY;
		
		float xRelativeToFirst = x2 - x1; 
		float yRelativeToFirst = y2 - y1;
		
		float pythagifiedLine = (xRelativeToFirst * xRelativeToFirst) + (yRelativeToFirst * yRelativeToFirst);
		
		pythagifiedLine = (float) Math.sqrt(pythagifiedLine);
		
		float percentOfLine = magnitude / pythagifiedLine;
		
		float xIterationSpeed = percentOfLine * xRelativeToFirst; 
		float yIterationSpeed = percentOfLine * yRelativeToFirst;
		
		return new Vector2(xIterationSpeed, yIterationSpeed);
	}
	
	/** Returns a vector containing the horizontal and vertical components of a vector in the direction of another point
	 * 
	 * @param originPoint the point of origin for the vector 
	 * @param directionalPoint the point that the vector is directed towards
	 * @param magnitude the magnitude of the vector 
	 */
	public static Vector2 vectorComponent(Vector2 originPoint, Vector2 directionalPoint, float magnitude) {
		return vectorComponent(originPoint.x, originPoint.y, directionalPoint.x, directionalPoint.y, magnitude);
	}
	
}
