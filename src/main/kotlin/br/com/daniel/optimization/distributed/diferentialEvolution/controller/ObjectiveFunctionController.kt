package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.CreateObjectiveFunctionRequest
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.CreateObjectiveFunctionResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.ErrorResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.GetObjectiveFunctionResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ObjectiveFunctionRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/objectiveFunction")
class ObjectiveFunctionController(
    val objectiveFunctionRepository: ObjectiveFunctionRepository
) {

    @GetMapping("/{objectiveFunctionId}")
    fun getObjectiveFunctionById(@PathVariable objectiveFunctionId: Long): ResponseEntity<GetObjectiveFunctionResponse> {
        val objectiveFunctionData = objectiveFunctionRepository.findById(objectiveFunctionId)
            .orElseThrow {
                RestHandledException(
                    ErrorResponse(NOT_FOUND.value(), "ObjectiveFunction with id $objectiveFunctionId not found")
                )
            }
        return ResponseEntity.ok(GetObjectiveFunctionResponse(objectiveFunctionData))
    }

    @PostMapping
    fun createObjectiveFunction(
        @RequestBody createObjectiveFunctionRequest: CreateObjectiveFunctionRequest
    ): ResponseEntity<CreateObjectiveFunctionResponse> {
        val objectiveFunctionData = objectiveFunctionRepository.save(createObjectiveFunctionRequest.toObjectiveFunctionData())
        return ResponseEntity
            .status(CREATED)
            .body(CreateObjectiveFunctionResponse(objectiveFunctionData))
    }

}