package org.joltsphere.misc

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape

class EllipseFixture {
    companion object {

        private var hW: Float = 0.toFloat()
        private var hH: Float = 0.toFloat()

        /**Creates an ellipse, this is a convenience method that creates a fixture defintion based off of the paramaters. */
        fun createEllipseFixtures(body: Body, density: Float, restitution: Float, friction: Float, halfWidth: Float, halfHeight: Float, userData: String): Body {
            val fdef = FixtureDef()
            fdef.restitution = restitution
            fdef.density = density
            fdef.friction = friction
            return createEllipseFixtures(body, fdef, halfWidth, halfHeight, userData)
        }

        /**Creates four fixtures in the shape of an ellipse based off of these paramaters */
        fun createEllipseFixtures(body: Body, fdef: FixtureDef, halfWidth: Float, halfHeight: Float, userData: String): Body {
            // if the ellipse is long and skinny, then for approximation, the ellipse is rotated then rotated back
            var shouldRotate = false
            if (halfWidth > halfHeight) {
                hW = halfWidth
                hH = halfHeight
            } else {
                hH = halfWidth
                hW = halfHeight
                shouldRotate = true
            }

            val poly = PolygonShape()

            val perimeter = (2.0 * Math.PI * Math.sqrt((Math.pow(hW.toDouble(), 2.0) + Math.pow(hH.toDouble(), 2.0)) / 2f)).toFloat() // perimeter of the ellipse
            val faceLength = perimeter / 4f / 6f // splits into 4 quadrants, then into 6 faces

            val v = arrayOfNulls<Vector2>(8) // max 8 vertices in a box2d polygon

            v[0] = Vector2(0f, 0f) // sets first point at center
            v[1] = Vector2(hW, 0f) // set the second point on half the width

            var prevX = hW
            var prevY = 0f // previous point on ellipse in loop
            var x = 0f
            var y = 0f

            for (i in 1..5) { // for the next 5 points

                val dtl = 10f // detail of calculations
                val iterationSize = faceLength / dtl // the x axis iteration length, set to face length because it will not exceed the hypotenuse
                val tinyIterationSize = iterationSize / dtl // once large scale iteration finds an approximated point, the tiny iteration loop does it again to be more precise
                // this algorithim would take the approximation power requirement down from dtl^2 to dtl*2 which is pretty great
                var prevTempX = 0f
                var j = 1
                while (j <= dtl) {
                    val currentW = iterationSize * j // gets current width of triangle
                    val currentH = ellipseFunction(prevX - currentW) - prevY // current height of face triangle
                    if (Math.hypot(currentW.toDouble(), currentH.toDouble()) > faceLength) break // discontinues all code within this for loop
                    prevTempX = prevX - currentW // the coordinates of this iteration, if still looping
                    j++
                }
                var n = 1
                while (n <= dtl) {
                    val currentW = prevX - prevTempX + tinyIterationSize * n // gets current precise width of the face triangle
                    val currentH = ellipseFunction(prevTempX - tinyIterationSize * n) - prevY // current precise height of the face triangle
                    if (Math.hypot(currentW.toDouble(), currentH.toDouble()) > faceLength) break // hypotenuse of imaginary triangle has exceeded facelength, so it can stop now
                    x = prevX - currentW
                    y = ellipseFunction(x) // sets the coordinates of the new face every loop
                    n++
                }
                prevX = x
                prevY = y // set the new starting point for the next vertex approximation
                v[i + 1] = Vector2(x, y)

            }

            v[7] = Vector2(0f, hH)

            if (shouldRotate) { // rotates all the vectors back to their appropriate loacation
                for (i in 0..7) {
                    v[i] = Vector2(v[i]!!.rotate90(1))
                }
            }

            poly.set(v)
            fdef.shape = poly
            body.createFixture(fdef).userData = userData // top right fixture

            for (i in 0..7) {
                v[i] = Vector2(v[i]!!.x * -1, v[i]!!.y) // flips each vector across the x axis
            }
            poly.set(v)
            fdef.shape = poly
            body.createFixture(fdef).userData = userData // top left fixture

            for (i in 0..7) {
                v[i] = Vector2(v[i]!!.x, v[i]!!.y * -1) // flips the previous fixture across the y axis
            }
            poly.set(v)
            fdef.shape = poly
            body.createFixture(fdef).userData = userData // bottom left fixture

            for (i in 0..7) {
                v[i] = Vector2(v[i]!!.x * -1, v[i]!!.y) // flips the previous vector across the x axis
            }
            poly.set(v)
            fdef.shape = poly
            body.createFixture(fdef).userData = userData // bottom right fixture
            poly.dispose()

            return body
        }

        private fun ellipseFunction(x: Float): Float {
            return Math.sqrt(Math.pow(hH.toDouble(), 2.0) * (1 - Math.pow(x.toDouble(), 2.0) / Math.pow(hW.toDouble(), 2.0))).toFloat() // function for half an ellipse
        }
    }

}