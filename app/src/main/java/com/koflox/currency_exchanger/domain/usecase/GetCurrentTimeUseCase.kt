package com.koflox.currency_exchanger.domain.usecase

interface GetCurrentTimeUseCase {
    suspend fun getCurrentTimeMillis(): Long
}

internal class GetCurrentTimeUseCaseImpl : GetCurrentTimeUseCase {
    override suspend fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
}
