import client.getNotEvaluatedChromosome
import client.publishEvaluationError
import client.publishEvaluationResult
import client.response.OptimizationStatus
import plane.elements.Point2D
import utils.getOptimizationRunIdFromFile
import utils.generatePoints

fun main(args: Array<String>) {

//    val controlPoints = listOf(
//        Point2D(0.0, 0.0),
//        Point2D(0.1,0.1),
//        Point2D(0.5,0.15),
//        Point2D(0.75,0.05),
//        Point2D(1.0,0.0)
//    )

//    pythonExecution {
//        val figure = figure()
//        val axes = figure.add_subplot()
//        sinSpacedTopPointsAirfoil.addPlotCommands(figure = figure, axes = axes, kwargs = mapOf(marker to Quoted("o")))
//        sinSpacedTopPointsBezierCurve.addPlotCommands(figure = figure, axes = axes, kwargs = mapOf(marker to Quoted("o")))
//        axes.legend(listOf("top surface true", "top surface bezier"))
//        xlim(xmin = 0.0, xmax = 1.0)
//        ylim(ymin = -0.5, ymax = 0.5)
//        grid()
//        show()
//    }


    print("Start app")
    while (true) {
        val optimizationRunId = getOptimizationRunIdFromFile()
        try {
            val (optimizationRunResponse, shouldWait) = getNotEvaluatedChromosome(optimizationRunId)

            if (shouldWait) {
                Thread.sleep(500)
                continue
            } else if (optimizationRunResponse!!.optimizationStatus != OptimizationStatus.FINISHED) {
                if (optimizationRunResponse.chromosome == null) throw java.lang.RuntimeException("null chromosome")
                val chromosome = optimizationRunResponse.chromosome

                try {
                    val fitness = objectiveFunction(chromosome.elements)
                    publishEvaluationResult(fitness, optimizationRunResponse.chromosome.id, optimizationRunResponse.chromosome.evaluationId!!)
                } catch (e: Exception) {
                    publishEvaluationError(chromosome.id, chromosome.evaluationId!!, "error while calculating objective function")
                }
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
}

