package org.joltsphere.misc

class ObjectData(var string: String) {
    var player = -1 // -1 by default for no players
    var count: Int = 0
    var isDead = false
        private set

    constructor(string: String, player: Int) : this(string) {
        this.player = player
    }

    constructor(count: Int, string: String) : this(string) {
        this.count = count
    }

    fun destroy() {
        isDead = true
    }

    val isPlayer: Boolean
        get() {
            if (player == -1)
                return false
            else
                return true
        }

}
