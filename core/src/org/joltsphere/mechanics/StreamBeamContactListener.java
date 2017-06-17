package org.joltsphere.mechanics;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class StreamBeamContactListener implements ContactListener {
	
	private Fixture fa, fb;
	
	public int streamBeamGroundContacts = 0, mountainClimberGroundContacts = 0;
	
	public StreamBeamContactListener() {
		
	}
	
	public void beginContact(Contact contact) {
		
		fa = contact.getFixtureA();
		fb = contact.getFixtureB();
		
		if (isContacting("mountainClimber", "ground")) mountainClimberGroundContacts++;
		if (isContacting("streamBeam", "ground")) streamBeamGroundContacts++;
		
	}
	
	public void endContact(Contact contact) {
		
		fa = contact.getFixtureA();
		fb = contact.getFixtureB();
		
		if (isContacting("mountainClimber", "ground")) mountainClimberGroundContacts--;
		if (isContacting("streamBeam", "ground")) streamBeamGroundContacts--;
		
	}

	public void preSolve(Contact contact, Manifold oldManifold) {}

	public void postSolve(Contact contact, ContactImpulse impulse) {}
	
	private boolean isContacting(String f1, String f2) {
		 if (fa.getUserData() == f1 && fb.getUserData() == f2)
			return true;
		else if (fa.getUserData() == f2 && fb.getUserData() == f1)
			return true;
		else return false;
	}
	
}
