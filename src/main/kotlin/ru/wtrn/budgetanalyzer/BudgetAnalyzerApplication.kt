package ru.wtrn.budgetanalyzer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling
import ru.wtrn.telegram.configuration.TelegramConfiguration

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@Import(TelegramConfiguration::class)
class BudgetAnalyzerApplication

fun main(args: Array<String>) {
	runApplication<BudgetAnalyzerApplication>(*args)
}
