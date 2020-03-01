package ru.wtrn.telegram.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.slf4j.MDCContext
import mu.KotlinLogging
import org.slf4j.MDC
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitExchange
import ru.wtrn.telegram.dto.request.SendMessageRequest
import java.util.UUID
import javax.xml.bind.JAXBElement

class TelegramMessageService(
    private val webClient: WebClient
) {
    private val logger = KotlinLogging.logger {  }

    suspend fun sendMessage(chat: Int, text: String) {
        val messageId = UUID.randomUUID()
        GlobalScope.async {
            logger.info { "[$messageId] Sending telegram message to $chat: $text" }
            val request = SendMessageRequest(
                chatId = chat,
                text = text
            )
            webClient.post()
                .uri("sendMessage")
                .bodyValue(request)
                .awaitExchange()
            logger.info { "[$messageId] Telegram message sent $chat: $text" }
        }
    }
}
