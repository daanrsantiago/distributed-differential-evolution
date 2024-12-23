import plane.CubicBezierSpline2D
import plane.elements.Point2D
import plane.elements.SmoothCubicBezierSplineControlPoints
import plane.functions.CubicSpline
import units.Angle
import utils.linspace
import utils.readAirfoilFile
import utils.sinSpace
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

fun objectiveFunction(designVariables: List<Double>, airfoilFileName: String = "clarky-ii.dat"): Double {
    val (topPoints, bottomPoints) = readAirfoilFile(airfoilFileName)
    val topPointsCubicSpline = CubicSpline(topPoints)

    val bezierCurve = buildSmoothCubicBezierSpline(designVariables, topPointsCubicSpline)
    val bezierCurvePoints = bezierCurve(linspace(0.0,1.0,150))
    var error = 0.0

    bezierCurvePoints.forEach {
        error += (topPointsCubicSpline(it.x) - it.y).pow(2)
    }

    return sqrt(error)
}

fun buildSmoothCubicBezierSpline(designVariables: List<Double>, topPointsCubicSpline: CubicSpline): CubicBezierSpline2D {
    val smoothCubicBezierSplineControlPoints = mutableListOf<SmoothCubicBezierSplineControlPoints>()

    val nPoints = (designVariables.size - 1)/3
    val tPoints = sinSpace(min = 3* PI /2, max = 2* PI, nPoints = nPoints).map { it + 1 }

    for (iPoint in 0 until nPoints ) {
        val pointOnCurve = Point2D(tPoints[iPoint], topPointsCubicSpline(tPoints[iPoint]))
        val angle: Angle
        val distanceControlPointAfter: Double?
        val distanceControlPointBefore: Double?
        if (iPoint == 0) {
            angle = Angle.Degrees(designVariables[0])
            distanceControlPointBefore = null
            distanceControlPointAfter = designVariables[1]
        } else if(iPoint == nPoints - 1) {
            angle = Angle.Degrees(designVariables[designVariables.lastIndex - 1])
            distanceControlPointBefore = designVariables.last()
            distanceControlPointAfter = null
        } else {
            angle = Angle.Degrees(designVariables[iPoint*3 - 1])
            distanceControlPointBefore = designVariables[iPoint*3]
            distanceControlPointAfter = designVariables[iPoint*3 + 1]
        }
        smoothCubicBezierSplineControlPoints.add(
            SmoothCubicBezierSplineControlPoints(
                pointOnCurve,
                angle,
                distanceControlPointBefore,
                distanceControlPointAfter
            )
        )
    }

    return CubicBezierSpline2D(smoothCubicBezierSplineControlPoints)
}