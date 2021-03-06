package ru.wtrn.budgetanalyzer.service

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.wtrn.budgetanalyzer.model.Amount
import java.math.BigDecimal
import java.util.Currency

@Service
class CurrentLimitsNotifier(
    private val limitsService: LimitsService,
    private val notificationsService: NotificationsService
) {
    private val logger = KotlinLogging.logger {  }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Moscow")
    fun sendCurrentLimitsNotification() = runBlocking {
        logger.trace { "Sending current limits notification" }
        val resultingLimits = limitsService.increaseSpentAmount(Amount(BigDecimal.ZERO, Currency.getInstance("RUB")));
        logger.trace { "Resulting limits: $resultingLimits" }
        notificationsService.notifyScheduledLimitsUpdate(resultingLimits)
    }
}
