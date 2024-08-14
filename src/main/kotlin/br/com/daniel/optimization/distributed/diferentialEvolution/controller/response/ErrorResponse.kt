package br.com.daniel.optimization.distributed.diferentialEvolution.controller.response

data class ErrorResponse (
    val code: Int = 500,
    val message: String? = null
)