package org.joltshpere.testing.mechanics;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class TestingContactListener implements ContactListener {
	
	public byte player1Contact = 0;
	public byte player2Contact = 0; 
	
	public void beginContact(Contact contact) {
		
		Fixture fa = contact.getFixtureA();
		Fixture fb = contact.getFixtureB();
		
		if (fa.getUserData().equals("ground") && fb.getUserData().equals("p1")) {
			player1Contact++;
		}
		
		if (fa.getUserData().equals("ground") && fb.getUserData().equals("p2")) {
			player2Contact++;
		}

		//System.out.println(fa.getUserData() + ", " + fb.getUserData());
		//System.out.println("P1: " + player1Contact +  ", P2: " + player2Contact + "; " + fa.getUserData() + ", " + fb.getUserData() );
	
	}
	
	public void endContact(Contact contact) {
	
		Fixture fa = contact.getFixtureA();
		Fixture fb = contact.getFixtureB();
		
		if (fa.getUserData().equals("ground") && fb.getUserData().equals("p1")) {
			player1Contact--;
		}
		
		if (fa.getUserData().equals("ground") && fb.getUserData().equals("p2")) {
			player2Contact--;
		}
		
	}

	public void preSolve(Contact contact, Manifold oldManifold) {
				
	}

	public void postSolve(Contact contact, ContactImpulse impulse) {
		
	}
	
}
