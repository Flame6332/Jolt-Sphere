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

fun Int.toF(): Float = this.toFloat()
fun Double.toF(): Float = this.toFloat()

/*val inputs = createMatrix(
        row(0f,0f,1f),
        row(0f,1f,1f),
        row(1f,0f,1f),
        row(1f,1f,1f))*/
fun createMatrix(vararg rows: FloatArray): Array<FloatArray> = rows as Array<FloatArray>
fun row(vararg x: Float): FloatArray = x
fun randomMatrix(rows: Int, columns: Int): Array<FloatArray> {
    val matrix = Array(rows, {FloatArray(columns)})
    for (i in 0 until rows)
        for (j in 0 until columns)
            matrix[i][j] = Math.random().toFloat()
    return matrix
}
fun matrixMultiply(matrix1: Array<FloatArray>, matrix2: Array<FloatArray>): Array<FloatArray> {
    val product = Array(rows(matrix1), {FloatArray(columns(matrix2))})
    for (i in 0 until matrix1.size)
        for (j in 0 until matrix2.first().size)
            for (k in 0 until matrix1.first().size)
                product[i][j] += matrix1[i][k] * matrix2[k][j]
    return product
}
fun scalarAdd(scalar: Float, matrix: Array<FloatArray>): Array<FloatArray> {
    val output = Array(rows(matrix), {FloatArray(columns(matrix))})
    for (i in 0 until rows(matrix))
        for (j in 0 until columns(matrix))
            output[i][j] = matrix[i][j] + scalar
    return output
}
fun scalarMultiply(scalar: Float, matrix: Array<FloatArray>): Array<FloatArray> {
    val output = Array(rows(matrix), {FloatArray(columns(matrix))})
    for (i in 0 until rows(matrix))
        for (j in 0 until columns(matrix))
            output[i][j] = matrix[i][j] * scalar
    return output
}
fun scalarDivide(scalar: Float, matrix: Array<FloatArray>): Array<FloatArray> {
    val output = Array(rows(matrix), {FloatArray(columns(matrix))})
    for (i in 0 until rows(matrix))
        for (j in 0 until columns(matrix))
            output[i][j] = scalar / matrix[i][j]
    return output
}
fun scalarExponent(scalar: Float, matrix: Array<FloatArray>): Array<FloatArray> {
    val output = Array(rows(matrix), {FloatArray(columns(matrix))})
    for (i in 0 until rows(matrix))
        for (j in 0 until columns(matrix))
            output[i][j] = Math.pow(scalar.toDouble(), matrix[i][j].toDouble()).toFloat()
    return output
}
fun scalarExponent(matrix: Array<FloatArray>, scalar: Float): Array<FloatArray> {
    val output = Array(rows(matrix), {FloatArray(columns(matrix))})
    for (i in 0 until rows(matrix))
        for (j in 0 until columns(matrix))
            output[i][j] = Math.pow(matrix[i][j].toDouble(), scalar.toDouble()).toFloat()
    return output
}
fun dotAdd(matrix1: Array<FloatArray>, matrix2: Array<FloatArray>): Array<FloatArray> {
    checkMatrixSizes(matrix1, matrix2)
    val output = Array(rows(matrix1), {FloatArray(columns(matrix1))})
    for (i in 0 until rows(matrix1))
        for (j in 0 until columns(matrix1))
            output[i][j] = matrix1[i][j] + matrix2[i][j]
    return output
}
fun dotSubtract(matrix1: Array<FloatArray>, matrix2: Array<FloatArray>): Array<FloatArray> {
    checkMatrixSizes(matrix1, matrix2)
    val output = Array(rows(matrix1), {FloatArray(columns(matrix1))})
    for (i in 0 until rows(matrix1))
        for (j in 0 until columns(matrix1))
            output[i][j] = matrix1[i][j] - matrix2[i][j]
    return output
}
fun dotDivide(matrix1: Array<FloatArray>, matrix2: Array<FloatArray>): Array<FloatArray> {
    checkMatrixSizes(matrix1, matrix2)
    val output = Array(rows(matrix1), {FloatArray(columns(matrix1))})
    for (i in 0 until rows(matrix1))
        for (j in 0 until columns(matrix1))
            output[i][j] = matrix1[i][j] / matrix2[i][j]
    return output
}
fun dotMultiply(matrix1: Array<FloatArray>, matrix2: Array<FloatArray>): Array<FloatArray> {
    checkMatrixSizes(matrix1, matrix2)
    val output = Array(rows(matrix1), {FloatArray(columns(matrix1))})
    for (i in 0 until rows(matrix1))
        for (j in 0 until columns(matrix1))
            output[i][j] = matrix1[i][j] * matrix2[i][j]
    return output
}
fun transpose(matrix: Array<FloatArray>): Array<FloatArray> {
    val output = Array(columns(matrix), {FloatArray(rows(matrix))})
    for (i in 0 until rows(matrix))
        for (j in 0 until columns(matrix))
            output[j][i] = matrix[i][j]
    return output
}
fun matrixAbsolute(matrix: Array<FloatArray>): Array<FloatArray> {
    val output = Array(rows(matrix), {FloatArray(columns(matrix))})
    for (i in 0 until rows(matrix))
        for (j in 0 until columns(matrix))
            output[i][j] = Math.abs(matrix[i][j])
    return output
}
fun matrixMean(matrix: Array<FloatArray>): Float {
    var mean = 0f
    for (i in 0 until rows(matrix))
        for (j in 0 until columns(matrix))
            mean += matrix[i][j]
    return mean / 4f
}
fun printMatrix(matrix: Array<FloatArray>) {
    for (row in matrix) {
        for (column in row)
            print("$column    ")
        println()
    }
}
fun checkMatrixSizes(matrix1: Array<FloatArray>, matrix2: Array<FloatArray>) {
    if (rows(matrix1) != rows(matrix2)) println("ERROR: ${rows(matrix1)} rows of matrix#1 != ${rows(matrix2)} rows of matrix#2")
    if (columns(matrix1) != columns(matrix2)) println("ERROR: ${columns(matrix1)} columns of matrix#1 != ${columns(matrix2)} columns of matrix#2")
}
fun rows(matrix: Array<FloatArray>): Int = matrix.size
fun columns(matrix: Array<FloatArray>): Int = matrix.first().size