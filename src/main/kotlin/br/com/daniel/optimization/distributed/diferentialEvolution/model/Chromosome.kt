package br.com.daniel.optimization.distributed.diferentialEvolution.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus
import java.time.ZonedDateTime

class Chromosome(
    val fitness: Double? = null,
    val size: Int,
    var evaluationStatus: EvaluationStatus = EvaluationStatus.NOT_EVALUATED,
    var evaluationId: String? = null,
    val elements: MutableList<Double>? = null,
    var evaluationBeginAt: ZonedDateTime? = null,
    var evaluatedAt: ZonedDateTime? = null,
) {

}
