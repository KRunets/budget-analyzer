package ru.wtrn.budgetanalyzer.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import ru.wtrn.budgetanalyzer.dto.kafka.BudgetStatusKafkaDto
import ru.wtrn.budgetanalyzer.repository.TransactionRepository
import ru.wtrn.budgetanalyzer.util.remainingAmount

@Service
class DashboardEventerService(
    private val kafkaTemplate: KafkaTemplate<Any, Any>,
    private val transactionRepository: TransactionRepository
) {
    private val logger = KotlinLogging.logger {  }

    suspend fun sendDashboardEvent(
        resultingLimits: LimitsService.ResultingLimits
    ) {
        val cardBalance = transactionRepository.getCurrentBalance()?.value

        val dto = BudgetStatusKafkaDto(
            balance = resultingLimits.budgetBalanceAmount.value,
            today = resultingLimits.todayLimit.remainingAmount.value,
            tomorrow = resultingLimits.nextDayCalculatedLimit.limitAmount.value,
            month = resultingLimits.monthLimit.remainingAmount.value,
            cardBalance = cardBalance
        )
        logger.trace { "Sending dashboard event $dto" }

        withContext(Dispatchers.IO) {
            kafkaTemplate.send("ru.wtrn.hs.dashboard.budget", dto)
        }

        logger.info { "Kafka event sent: $dto" }
    }
}
