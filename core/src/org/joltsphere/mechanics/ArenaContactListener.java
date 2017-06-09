package org.joltsphere.mechanics;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;

public class ArenaContactListener implements ContactListener {

	public Array<Integer> playerContacts;
	public byte pvpContact = 0;
	
	private Fixture fa, fb;
	
	public ArenaContactListener(int playerCount) {
		playerContacts = new Array<Integer>();
		for (int i = 1; i <= playerCount; i++) {
			playerContacts.add(0);
		}
	}
	
	public void beginContact(Contact contact) {
		
		fa = contact.getFixtureA();
		fb = contact.getFixtureB();
		
		for (int i = 1; i <= playerContacts.size; i++) {
			if (isContacting("ground", "p"+i)) playerContacts.set(i - 1, playerContacts.get(i - 1) + 1);
		}
		
		if (isContacting("p1", "p2")) pvpContact++;
	
	}
	
	public void endContact(Contact contact) {
		
		fa = contact.getFixtureA();
		fb = contact.getFixtureB();
		
		for (int i = 1; i <= playerContacts.size; i++) {
			if (isContacting("ground", "p"+i)) playerContacts.set(i - 1, playerContacts.get(i - 1) - 1);
		}
		
		if (isContacting("p1", "p2")) pvpContact--;
	
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
