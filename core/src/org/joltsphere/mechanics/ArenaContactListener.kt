package org.joltsphere.mechanics

import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Array

/* TODO
    * - Create pvp contacts array
    */
class ArenaContactListener(playerCount: Int) : ContactListener {

    var playerContacts: Array<Int> = Array<Int>()

    var pvpContact: Byte = 0

    private lateinit var bA: Body
    private lateinit var bB: Body

    init {
        for (i in 1..playerCount) {
            playerContacts.add(0)
        }
    }

    override fun beginContact(contact: Contact) {

        bA = contact.fixtureA.body
        bB = contact.fixtureB.body

        for (i in 1..playerContacts.size) {
            if (isContacting("ground", "p" + i)) playerContacts.set(i - 1, playerContacts.get(i - 1) + 1)
        }

        if (isContacting("p1", "p2")) pvpContact++

    }

    override fun endContact(contact: Contact) {

        bA = contact.fixtureA.body
        bB = contact.fixtureB.body

        for (i in 1..playerContacts.size) {
            if (isContacting("ground", "p" + i)) playerContacts.set(i - 1, playerContacts.get(i - 1) - 1)
        }

        if (isContacting("p1", "p2")) pvpContact--

    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {}

    override fun postSolve(contact: Contact, impulse: ContactImpulse) {}

    private fun isContacting(f1: String, f2: String): Boolean {
        if (bA.userData == f1 && bB.userData == f2)
            return true
        else if (bA.userData == f2 && bB.userData == f1)
            return true
        else
            return false
    }

}
