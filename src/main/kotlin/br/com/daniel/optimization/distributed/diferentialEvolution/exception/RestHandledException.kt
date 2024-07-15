package br.com.daniel.optimization.distributed.diferentialEvolution.exception

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.ErrorResponse

class RestHandledException(
    val errorResponse: ErrorResponse
) : RuntimeException(errorResponse.message) {
}