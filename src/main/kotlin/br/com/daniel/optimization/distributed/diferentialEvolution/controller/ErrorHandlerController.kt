package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.ErrorResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ErrorHandlerController {

    @ExceptionHandler(value = [ Exception::class ])
    fun handleGenericException(exception: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.internalServerError().body(ErrorResponse(500, exception.message))
    }

    @ExceptionHandler(value = [ RestHandledException::class ])
    fun handleRestException(restHandledException: RestHandledException): ResponseEntity<ErrorResponse> {
        val errorResponse = restHandledException.errorResponse
        return ResponseEntity.status(errorResponse.code).body(errorResponse)
    }
}