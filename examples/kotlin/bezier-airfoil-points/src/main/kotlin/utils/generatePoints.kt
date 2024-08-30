package utils

import plane.elements.Point2D

fun generatePoints(controlPointsCoordinates: List<Double>): MutableList<Point2D> {

    return controlPointsCoordinates.mapIndexedNotNull { index, _ ->
        if (index % 2 ==  1) {
            Point2D(controlPointsCoordinates[index-1], controlPointsCoordinates[index])
        } else {
            null
        }
    }.toMutableList()

}