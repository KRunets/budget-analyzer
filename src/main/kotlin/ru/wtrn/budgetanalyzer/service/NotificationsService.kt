package ru.wtrn.budgetanalyzer.service

import org.springframework.stereotype.Service
import ru.wtrn.budgetanalyzer.configuration.properties.BudgetAnalyzerTelegramProperties
import ru.wtrn.budgetanalyzer.entity.TransactionEntity
import ru.wtrn.budgetanalyzer.model.Amount
import ru.wtrn.budgetanalyzer.util.limitAmount
import ru.wtrn.budgetanalyzer.util.remainingAmount
import ru.wtrn.telegram.service.TelegramMessageService

@Service
class NotificationsService(
    private val telegramMessageService: TelegramMessageService,
    private val budgetAnalyzerTelegramProperties: BudgetAnalyzerTelegramProperties,
    private val dashboardEventerService: DashboardEventerService
) {
    suspend fun sendTransactionNotification(transactionEntity: TransactionEntity, resultingLimits: LimitsService.ResultingLimits) {
        val text = """
            Баланс: ${resultingLimits.budgetBalanceAmount}
            До конца месяца: ${resultingLimits.monthLimit.remainingAmount}  
            Сегодня: ${resultingLimits.todayLimit.remainingAmount}
            Завтра: ${resultingLimits.nextDayCalculatedLimit.limitAmount}
            Баланс карты: ${transactionEntity.remainingBalance}
            
            ${transactionEntity.amount} ${transactionEntity.merchant}
            """.trimIndent()

        dashboardEventerService.sendDashboardEvent(resultingLimits)

        telegramMessageService.sendMessage(
            chat = budgetAnalyzerTelegramProperties.targetChat,
            text = text
        )
    }

    suspend fun sendManualLimitUpdateNotification(
        amount: Amount,
        description: String?,
        resultingLimits: LimitsService.ResultingLimits
    ) {
        val text = """
            Баланс: ${resultingLimits.budgetBalanceAmount}
            До конца месяца: ${resultingLimits.monthLimit.remainingAmount}
            Сегодня: ${resultingLimits.todayLimit.remainingAmount} 
            Завтра: ${resultingLimits.nextDayCalculatedLimit.limitAmount}
            
            $amount ${description ?: ""}
            """.trimIndent()

        dashboardEventerService.sendDashboardEvent(resultingLimits)

        telegramMessageService.sendMessage(
            chat = budgetAnalyzerTelegramProperties.targetChat,
            text = text
        )
    }

    suspend fun notifyScheduledLimitsUpdate(
        resultingLimits: LimitsService.ResultingLimits
    ) {
        val text = """
            Баланс: ${resultingLimits.budgetBalanceAmount}
            До конца месяца: ${resultingLimits.monthLimit.remainingAmount}
            Сегодня: ${resultingLimits.todayLimit.remainingAmount} 
            Завтра: ${resultingLimits.nextDayCalculatedLimit.limitAmount}
            """.trimIndent()

        dashboardEventerService.sendDashboardEvent(resultingLimits)

        telegramMessageService.sendMessage(
            chat = budgetAnalyzerTelegramProperties.targetChat,
            text = text
        )
    }
}
