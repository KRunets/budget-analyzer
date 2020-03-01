package ru.wtrn.budgetanalyzer.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.wtrn.budgetanalyzer.configuration.properties.LimitsProperties
import ru.wtrn.budgetanalyzer.entity.CurrentLimitEntity
import ru.wtrn.budgetanalyzer.model.Amount
import ru.wtrn.budgetanalyzer.model.CalculatedDayLimit
import ru.wtrn.budgetanalyzer.repository.CurrentLimitRepository
import ru.wtrn.budgetanalyzer.util.atEndOfMonth
import ru.wtrn.budgetanalyzer.util.remainingAmount
import ru.wtrn.budgetanalyzer.util.spentAmount
import java.math.BigDecimal

@Service
class LimitsService(
    private val currentLimitRepository: CurrentLimitRepository,
    private val limitsProperties: LimitsProperties
) {
    private val logger = KotlinLogging.logger {  }

    suspend fun increaseSpentAmount(amount: Amount): ResultingLimits {
        logger.info { "Increasing spent amount: $amount" }

        val foundLimits = currentLimitRepository.findActiveLimits(
            currency = amount.currency
        )
            .associateBy { it.timespan }

        logger.trace { "Found limits: $foundLimits" }

        val monthLimit = (foundLimits[CurrentLimitEntity.LimitTimespan.MONTH] ?: constructMonthLimit())
        val dayLimit = foundLimits[CurrentLimitEntity.LimitTimespan.DAY] ?: constructDayLimit(monthLimit)

        if (amount.value != BigDecimal.ZERO) {
            currentLimitRepository.increaseSpentAmount(
                limitIds = listOf(monthLimit.id, dayLimit.id),
                amountValue = amount.value
            )

            listOf(dayLimit, monthLimit).forEach {
                it.spentValue += amount.value
            }
            logger.trace { "Limits updated: $monthLimit $dayLimit" }
        }

        val nextDay = dayLimit.periodStart.plusDays(1)
        val nextDayCalculatedLimit = when (nextDay.monthValue == monthLimit.periodStart.monthValue) {
            true -> CalculatedDayLimit.of(
                date = nextDay,
                spentValue = monthLimit.spentValue,
                limitValue = monthLimit.limitValue,
                currency = monthLimit.currency
            )
            false -> CalculatedDayLimit.of(
                date = nextDay,
                spentValue = BigDecimal.ZERO,
                limitValue = monthLimit.limitValue,
                currency = monthLimit.currency
            )
        }

        val budgetBalanceAmount = let {
            val endOfMonthDay = BigDecimal(monthLimit.periodStart.atEndOfMonth().dayOfMonth)
            val todayDay = BigDecimal(dayLimit.periodStart.dayOfMonth)
            val budgetBalanceValue = (monthLimit.limitValue / endOfMonthDay) * todayDay - monthLimit.spentValue
            Amount(
                value = budgetBalanceValue,
                currency = dayLimit.currency
            )
        }

        logger.trace { "Limits increase completed" }

        return ResultingLimits(
            todayLimit = dayLimit,
            monthLimit = monthLimit,
            nextDayCalculatedLimit = nextDayCalculatedLimit,
            budgetBalanceAmount = budgetBalanceAmount
        )
    }

    private suspend fun constructMonthLimit(): CurrentLimitEntity {
        val entity = CurrentLimitEntity.constructMonthLimit(
            limitAmount = Amount(
                value = limitsProperties.daily,
                currency = limitsProperties.currency
            ),
            timezone = limitsProperties.timezone
        )
        currentLimitRepository.insert(entity)
        return entity
    }

    private suspend fun constructDayLimit(monthLimit: CurrentLimitEntity): CurrentLimitEntity {
        val entity = CurrentLimitEntity.constructDayLimit(
            monthLimit = monthLimit
        )
        currentLimitRepository.insert(entity)
        return entity
    }

    data class ResultingLimits(
        val todayLimit: CurrentLimitEntity,
        val monthLimit: CurrentLimitEntity,
        val nextDayCalculatedLimit: CalculatedDayLimit,
        val budgetBalanceAmount: Amount
    )
}
