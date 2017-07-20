package org.joltsphere.mechanics

import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.utils.Array

/* TODO
    * - Create pvp contacts array
    */
class ArenaContactListener(playerCount: Int) : ContactListener {

    var playerContacts: Array<Int> = Array<Int>()

    var pvpContact: Byte = 0

    private var fa: Fixture? = null
    private var fb: Fixture? = null

    init {
        for (i in 1..playerCount) {
            playerContacts.add(0)
        }
    }

    override fun beginContact(contact: Contact) {

        fa = contact.fixtureA
        fb = contact.fixtureB

        for (i in 1..playerContacts.size) {
            if (isContacting("ground", "p" + i)) playerContacts.set(i - 1, playerContacts.get(i - 1) + 1)
        }

        if (isContacting("p1", "p2")) pvpContact++

    }

    override fun endContact(contact: Contact) {

        fa = contact.fixtureA
        fb = contact.fixtureB

        for (i in 1..playerContacts.size) {
            if (isContacting("ground", "p" + i)) playerContacts.set(i - 1, playerContacts.get(i - 1) - 1)
        }

        if (isContacting("p1", "p2")) pvpContact--

    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {}

    override fun postSolve(contact: Contact, impulse: ContactImpulse) {}

    private fun isContacting(f1: String, f2: String): Boolean {
        if (fa!!.userData == f1 && fb!!.userData == f2)
            return true
        else if (fa!!.userData == f2 && fb!!.userData == f1)
            return true
        else
            return false
    }

}
