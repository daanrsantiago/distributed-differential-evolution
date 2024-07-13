package br.com.daniel.optimization.distributed.diferentialEvolution

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class DistributedDifferentialEvolutionApplication

fun main(args: Array<String>) {
    runApplication<DistributedDifferentialEvolutionApplication>(*args)
}