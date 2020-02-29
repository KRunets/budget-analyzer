package ru.wtrn.budgetanalyzer.repository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitFirstOrNull
import org.springframework.stereotype.Repository
import ru.wtrn.budgetanalyzer.entity.TransactionEntity
import ru.wtrn.budgetanalyzer.model.Amount
import ru.wtrn.budgetanalyzer.support.CoroutineCrudRepository
import java.util.UUID

@Repository
class TransactionRepository(
    databaseClient: DatabaseClient,
    private val objectMapper: ObjectMapper
) : CoroutineCrudRepository<TransactionEntity, UUID>(
    domainType = TransactionEntity::class.java,
    idColumn = "id",
    databaseClient = databaseClient
) {
    suspend fun getCurrentBalance(): Amount? =
        databaseClient.execute(
            //language=PostgreSQL
            """
            SELECT remaining_balance
            FROM transactions
            ORDER BY created_at DESC
            LIMIT 1
        """.trimIndent()
        )
            .`as`(String::class.java)
            .fetch()
            .awaitFirstOrNull()
            ?.let {
                @Suppress("BlockingMethodInNonBlockingContext")
                objectMapper.readValue(it, Amount::class.java)
            }
}
