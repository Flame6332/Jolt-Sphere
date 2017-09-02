package org.joltsphere.misc

import com.badlogic.gdx.math.Vector2

class Misc {
    companion object {

        /** Just a float version of Math.random()  */
        fun random(): Float {
            return Math.random().toFloat()
        }

        /** Returns a random value between the min and max  */
        fun random(min: Float, max: Float): Float {
            return (max - min) * Math.random().toFloat() + min
        }

        /** Returns a random value that has been rounded to an integer  */
        fun randomInt(min: Int, max: Int): Int {
            return Math.round(random(min.toFloat(), max.toFloat()))
        }

        /** Returns a vector containing the horizontal and vertical components of a vector in the direction of another point

         * @param originX the x position of the vector origin
         * @param originY the y position of the vector origin
         * @param directionX the x position that the vector is directed towards
         * @param directionY the y position that the vector is directed towards
         * @param magnitude the magnitude of the given vector
         */
        fun vectorComponent(originX: Float, originY: Float, directionX: Float, directionY: Float, magnitude: Float): Vector2 {
            val x1 = originX
            val y1 = originY
            val x2 = directionX
            val y2 = directionY

            val xRelativeToFirst = x2 - x1
            val yRelativeToFirst = y2 - y1

            var pythagifiedLine = xRelativeToFirst * xRelativeToFirst + yRelativeToFirst * yRelativeToFirst

            pythagifiedLine = Math.sqrt(pythagifiedLine.toDouble()).toFloat()

            val percentOfLine = magnitude / pythagifiedLine

            val xIterationSpeed = percentOfLine * xRelativeToFirst
            val yIterationSpeed = percentOfLine * yRelativeToFirst

            return Vector2(xIterationSpeed, yIterationSpeed)
        }

        /** Returns a vector containing the horizontal and vertical components of a vector in the direction of another point

         * @param originPoint the point of origin for the vector
         * @param directionalPoint the point that the vector is directed towards
         * @param magnitude the magnitude of the vector
         */
        fun vectorComponent(originPoint: Vector2, directionalPoint: Vector2, magnitude: Float): Vector2 {
            return vectorComponent(originPoint.x, originPoint.y, directionalPoint.x, directionalPoint.y, magnitude)
        }


    }
}

class LastFrame {
    private var boolean = true
    /** Execute this function every time the action occurs */
    fun occured() {
        if (!boolean) boolean = true // optimizations lol, could of just set it to true and called it a day
    }
    /** Returns true if the action just ended, note that this will be false if called twice in a row */
    fun justEnded() : Boolean {
        if (boolean) {
            boolean = false
            return true
        }
        else return false
    }
}