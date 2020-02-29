package ru.wtrn.budgetanalyzer.service

import org.springframework.stereotype.Service
import ru.wtrn.budgetanalyzer.entity.ManualLimitUpdateEntity
import ru.wtrn.budgetanalyzer.model.Amount
import ru.wtrn.budgetanalyzer.repository.ManualLimitUpdateRepository
import java.math.BigDecimal

@Service
class ManualLimitUpdateService(
    private val manualLimitUpdateRepository: ManualLimitUpdateRepository,
    private val limitsService: LimitsService,
    private val notificationsService: NotificationsService
) {
    suspend fun increaseSpentAmount(
        amount: Amount,
        description: String?,
        user: String
    ) {
        val entity = ManualLimitUpdateEntity(
            amountValue = amount.value,
            description = description,
            author = user
        )
        manualLimitUpdateRepository.insert(entity)

        val resultingLimits = limitsService.increaseSpentAmount(amount)
        notificationsService.sendManualLimitUpdateNotification(
            amount = amount,
            description = description,
            resultingLimits = resultingLimits
        )
    }
}
