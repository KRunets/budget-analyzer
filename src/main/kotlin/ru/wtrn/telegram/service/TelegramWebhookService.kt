package ru.wtrn.telegram.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.wtrn.budgetanalyzer.configuration.properties.BudgetAnalyzerTelegramProperties
import ru.wtrn.budgetanalyzer.configuration.properties.LimitsProperties
import ru.wtrn.budgetanalyzer.model.Amount
import ru.wtrn.budgetanalyzer.service.LimitsService
import ru.wtrn.budgetanalyzer.service.NotificationsService
import ru.wtrn.telegram.configuration.properties.TelegramProperties
import ru.wtrn.telegram.dto.hook.TelegramUpdate
import ru.wtrn.telegram.exception.IncorrectWebhookKeyException
import java.lang.Exception
import java.math.BigDecimal

@Service
class TelegramWebhookService(
    private val telegramProperties: TelegramProperties,
    private val objectMapper: ObjectMapper,
    private val budgetAnalyzerTelegramProperties: BudgetAnalyzerTelegramProperties,
    private val telegramMessageService: TelegramMessageService,
    private val limitsService: LimitsService,
    private val limitsProperties: LimitsProperties,
    private val notificationsService: NotificationsService
) {
    private val logger = KotlinLogging.logger {  }

    @Suppress("DeferredResultUnused")
    suspend fun handleWebhook(
        webhookKey: String,
        requestBody: String
    ) {
        if (webhookKey != telegramProperties.webhookKey) {
            throw IncorrectWebhookKeyException()
        }
        val update = objectMapper.readValue<TelegramUpdate>(requestBody)
        update.message?.let {
            handleMessage(it)
        }
    }

    suspend fun handleMessage(message: TelegramUpdate.Message) {
        if (message.chat.id != budgetAnalyzerTelegramProperties.targetChat) {
            telegramMessageService.sendMessage(message.chat.id, "This chat is not whitelisted")
        }
        handleManualLimitDecreaseMessage(message)
    }

    private suspend fun handleManualLimitDecreaseMessage(message: TelegramUpdate.Message) {
        val (amountValue, description) = try {
            val parts = message.text.split(" ", limit = 2)
            val amountValue = BigDecimal(parts.first())
            val description = parts.getOrNull(1)
            amountValue to description
        } catch (e: Exception) {
            // Let's pretend nothing happened. Probably message was not addressed to bot.
            logger.trace(e) { "Failed to parse received message" }
            return
        }

        val amount = Amount(
            value = amountValue,
            currency = limitsProperties.currency
        )

        val resultingLimits = limitsService.increaseSpentAmount(amount)
        notificationsService.sendManualLimitUpdateNotification(
            amount = amount,
            description = description,
            resultingLimits = resultingLimits
        )
    }
}
