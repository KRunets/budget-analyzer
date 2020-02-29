package ru.wtrn.budgetanalyzer.service

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
        kafkaTemplate.send("ru.wtrn.hs.dashboard.budget", dto)
    }
}
