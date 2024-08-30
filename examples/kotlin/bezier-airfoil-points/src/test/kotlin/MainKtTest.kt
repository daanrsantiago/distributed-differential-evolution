import io.github.danielTucano.matplotlib.KwargValue
import io.github.danielTucano.matplotlib.Line2D
import io.github.danielTucano.matplotlib.pyplot.figure
import io.github.danielTucano.matplotlib.pyplot.grid
import io.github.danielTucano.matplotlib.pyplot.xlim
import io.github.danielTucano.matplotlib.pyplot.ylim
import io.github.danielTucano.matplotlib.show
import io.github.danielTucano.python.pythonExecution
import org.junit.jupiter.api.Test
import plane.addPlotCommands
import plane.functions.CubicSpline
import plane.utils.toPoint2DList
import utils.linspace
import utils.readAirfoilFile
import utils.sinSpace
import kotlin.math.PI

internal class MainKtTest {

    @Test
    fun seeResults() {
        val sinSpaceXValues = sinSpace(min = 3* PI /2, max = 2* PI, nPoints = 100).map { it + 1 }

        val (topPoints, bottomPoints) = readAirfoilFile("clarky-ii.dat")
        val topPointsSpline = CubicSpline(topPoints)
        val sinSpacedTopPointsAirfoil = (sinSpaceXValues to topPointsSpline(sinSpaceXValues)).toPoint2DList()

        val bezierCurve = buildSmoothCubicBezierSpline(
            listOf(
                87.88124338843572,
                0.0096223953893825,
                30.15158544693709,
                0.012414017484230376,
                0.034028797580023966,
                14.097122861297633,
                0.04247727499284723,
                0.06690987169994203,
                2.642256420022008,
                0.022527368145839877,
                0.07973005247706386,
                -4.796599590655985,
                0.03897798423588234,
                0.09550233251045087,
                -9.387492241754625,
                0.09631053448549162,
                0.0956408857775513,
                -16.565670539836194,
                0.03639006590853443,
                0.09790643192340878,
                -15.329707848786569,
                0.020172133104562645
            ),
            topPointsSpline
        )
        val bezierCurvePoints = bezierCurve(linspace(0.0,1.0,150))
        val cubicSplineBezierCurve = CubicSpline(bezierCurvePoints)
        val sinSpacedTopPointsBezierCurve = (sinSpaceXValues to cubicSplineBezierCurve(sinSpaceXValues)).toPoint2DList()

        pythonExecution {
            val figure = figure()
            val axes = figure.add_subplot()
            sinSpacedTopPointsAirfoil.addPlotCommands(figure = figure, axes = axes, kwargs = mapOf(Line2D.Line2DArgs.marker to KwargValue.Quoted("o")))
            bezierCurve.addPlotCommands(figure = figure, axes = axes)
            axes.legend(listOf("top surface true", "top surface bezier control points", "top surface bezier"))
            xlim(xmin = 0.0, xmax = 1.0)
            ylim(ymin = -0.5, ymax = 0.5)
            grid()
            show()
        }
    }

}