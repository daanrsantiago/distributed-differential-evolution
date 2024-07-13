package br.com.daniel.optimization.distributed.diferentialEvolution.database.model

import jakarta.persistence.*
import jakarta.persistence.GenerationType.SEQUENCE
import java.time.ZoneId
import java.time.ZonedDateTime


@Entity
@Table(name = "objectiveFunctions")
class ObjectiveFunctionData(
    @Id
    @GeneratedValue(strategy = SEQUENCE)
    val id: Long? = null,
    val name: String? = null,
    val hash: String? = null,
    val description: String? = null,
    val createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
) {

}