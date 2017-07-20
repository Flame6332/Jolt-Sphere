package org.joltsphere.mechanics

import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.Manifold

class StreamBeamContactListener : ContactListener {

    private var fa: Fixture? = null
    private var fb: Fixture? = null

    var streamBeamGroundContacts = 0
    var mountainClimberGroundContacts = 0

    override fun beginContact(contact: Contact) {

        fa = contact.fixtureA
        fb = contact.fixtureB

        if (isContacting("mountainClimber", "ground")) mountainClimberGroundContacts++
        if (isContacting("streamBeam", "ground")) streamBeamGroundContacts++

    }

    override fun endContact(contact: Contact) {

        fa = contact.fixtureA
        fb = contact.fixtureB

        if (isContacting("mountainClimber", "ground")) mountainClimberGroundContacts--
        if (isContacting("streamBeam", "ground")) streamBeamGroundContacts--

    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {}

    override fun postSolve(contact: Contact, impulse: ContactImpulse) {}

    private fun isContacting(f1: String, f2: String): Boolean {
        if (fa!!.userData === f1 && fb!!.userData === f2)
            return true
        else if (fa!!.userData === f2 && fb!!.userData === f1)
            return true
        else
            return false
    }

}
