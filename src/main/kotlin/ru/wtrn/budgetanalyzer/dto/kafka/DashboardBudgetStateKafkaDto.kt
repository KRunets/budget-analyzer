package ru.wtrn.budgetanalyzer.dto.kafka

import java.math.BigDecimal

data class BudgetStatusKafkaDto(
    val balance: BigDecimal,
    val today: BigDecimal,
    val tomorrow: BigDecimal,
    val month: BigDecimal,
    val cardBalance: BigDecimal?
)
