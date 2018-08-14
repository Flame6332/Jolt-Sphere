package org.joltsphere.mechanics.attacks

class ArenaPlayerAttack {

    fun activate() {}
    fun update() {}

}

/*
    *
    *  Attack structure:
    *       Two parts of an attack: the summoned, and the transformative.
    *
    *       The summoned portion is new bodies that
    *       are not controllable by the player and are separate entities that have their own despawn timer.
    *       Summoned entities are added to an array of despawn timers as they maybe summoned again before the
    *       previous entities are destroyed.
    *
    *       Transformative timers are singular and are reset at the end of any transformation. Transformations can not
    *       overlap.
    *
    *
    *  All attacks are constantly being updated by the external attack manager.
    *
    *  Attack manager will take an array of attacks and constantly be beginning attacks and ending them when necessary.
    *

    > Energy charged up sufficently for at least level one attack

    > User clicks down to initiate attack

    > Attack system checks which point in attack timeline he is in

    > If user isn't in attack sequence, begin level one attack

    > Attack manager begins appropriate level 1 attack, allowing the attack subclass
        to start transformation timer and to summon any entity objects.

    > Continues to update through the transformative portion of attack, awaiting either timer to
        run out or for attack manager to call end (which does nothing if attack already ends)

    > End attack

    >

 */