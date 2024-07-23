package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.request.CreateObjectiveFunctionRequest
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.CreateObjectiveFunctionResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.ErrorResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.GetObjectiveFunctionResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ObjectiveFunctionRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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