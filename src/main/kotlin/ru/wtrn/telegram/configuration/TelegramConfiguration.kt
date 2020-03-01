package ru.wtrn.telegram.configuration

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.ProxyProvider
import ru.wtrn.telegram.configuration.properties.TelegramProperties
import ru.wtrn.telegram.service.TelegramMessageService
import ru.wtrn.telegram.service.TelegramWebhookService
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(TelegramProperties::class)
@Import(TelegramWebhookService::class)
class TelegramConfiguration(
    private val telegramProperties: TelegramProperties
) {
    val webClient = let {
        var httpClient = HttpClient.create()
            .tcpConfiguration {
                it.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60_000)
                    .doOnConnected { connection ->
                        connection.addHandlerLast(ReadTimeoutHandler(60_000, TimeUnit.MILLISECONDS))
                    }
                    .doOnDisconnected { connection ->
                        connection.dispose()
                    }
            }
        telegramProperties.proxy.let { proxy ->
            httpClient = httpClient.tcpConfiguration {
                it.proxy {
                    it.type(ProxyProvider.Proxy.HTTP)
                        .host(proxy.host)
                        .port(proxy.port)
                }
            }
        }
        WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl("https://api.telegram.org/bot${telegramProperties.botKey}/")
            .build()
    }

    @Bean
    fun telegramMessageService(): TelegramMessageService = TelegramMessageService(
        webClient = webClient
    )
}
