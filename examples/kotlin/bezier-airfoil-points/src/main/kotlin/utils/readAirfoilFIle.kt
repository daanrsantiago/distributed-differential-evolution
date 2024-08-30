package utils

import plane.elements.Point2D
import java.lang.Double

fun readAirfoilFile(name: String): Pair<MutableList<Point2D>,MutableList<Point2D>> {

    val classloader = Thread.currentThread().getContextClassLoader();
    val topPoints = mutableListOf<Point2D>()
    val bottomPoints = mutableListOf<Point2D>()

    try {
        val file = classloader.getResourceAsStream(name)
        val bufferReader = file?.bufferedReader()

        val airfoilName = bufferReader?.readLine()
        bufferReader?.readLine()
        bufferReader?.readLine()
        var line: String? = ""
        var bottomPointsStarted = false

        while (line != null) {
            line = bufferReader?.readLine()

            if (line == null) break
            if (line.isBlank()) {
                bottomPointsStarted = true
                continue
            }

            val x = Double.parseDouble(line.substring(1,10))
            val y =Double.parseDouble(line.substring(11,20))

            if (bottomPointsStarted) {
                bottomPoints.add(Point2D(x,y))
            } else {
                topPoints.add(Point2D(x,y))
            }
        }

    } catch (e: Exception) {
        println(e)
    }

    return Pair(topPoints, bottomPoints)
}