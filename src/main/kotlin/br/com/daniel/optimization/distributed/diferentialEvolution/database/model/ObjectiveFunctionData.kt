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
    var name: String? = null,
    var hash: String? = null,
    var description: String? = null,
    val createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
) {

}