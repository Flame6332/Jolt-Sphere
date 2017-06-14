package org.joltsphere.mechanics;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class StreamBeamContactListener implements ContactListener {
	
	private Fixture fa, fb;
	
	public StreamBeamContactListener() {
		
	}
	
	public void beginContact(Contact contact) {
		
		fa = contact.getFixtureA();
		fb = contact.getFixtureB();
		
	
	}
	
	public void endContact(Contact contact) {
		
		fa = contact.getFixtureA();
		fb = contact.getFixtureB();
		
	}

	public void preSolve(Contact contact, Manifold oldManifold) {}

	public void postSolve(Contact contact, ContactImpulse impulse) {}
	
	private boolean isContacting(String f1, String f2) {
		 if (fa.getUserData().equals(f1) && fb.getUserData().equals(f2))
			return true;
		else if (fa.getUserData().equals(f2) && fb.getUserData().equals(f1))
			return true;
		else return false;
	}
	
}
