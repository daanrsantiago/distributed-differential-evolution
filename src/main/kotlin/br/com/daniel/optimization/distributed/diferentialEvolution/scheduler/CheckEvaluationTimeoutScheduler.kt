package br.com.daniel.optimization.distributed.diferentialEvolution.scheduler

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.EVALUATING
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.TIMEOUT
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus.RUNNING
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ChromosomeRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.OptimizationRunRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class CheckEvaluationTimeoutScheduler(
    val optimizationRunRepository: OptimizationRunRepository,
    val chromosomeRepository: ChromosomeRepository
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedRate = 1000)
    fun checkEvaluationTimeout() {
        val runningOptimizations = optimizationRunRepository.findAllByStatus(RUNNING)
        val currentDateTime = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))

        runningOptimizations.forEach { runningOptimization ->
            val runningOptimizationChromosomes = chromosomeRepository
                .findAllByOptimizationRunIdAndEvaluationStatus(runningOptimization.id!!, EVALUATING)

            runningOptimizationChromosomes.forEach { runningOptimizationChromosome ->
                val evaluationTimeout = currentDateTime.isAfter(
                    runningOptimizationChromosome.evaluationBeginAt!!.plus(
                        runningOptimization.objectiveFunctionEvaluationTimeoutSeconds!!,
                        ChronoUnit.SECONDS
                    )
                )
                if (evaluationTimeout) {
                    logger.info("Chromosome with id ${runningOptimizationChromosome.id} from optimizationRun with id ${runningOptimization.id} changed evaluationStatus to TIMEOUT")
                    runningOptimizationChromosome.evaluationStatus = TIMEOUT
                    chromosomeRepository.save(runningOptimizationChromosome)
                }
            }
        }

    }
}