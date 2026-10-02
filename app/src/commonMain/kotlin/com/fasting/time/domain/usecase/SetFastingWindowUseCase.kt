package com.fasting.time.domain.usecase

import com.fasting.time.domain.model.FastingWindow
import com.fasting.time.domain.repository.FastingWindowRepository

/** Chooses the window to fast in, in place of the one chosen before. */
class SetFastingWindowUseCase(private val fastingWindowRepository: FastingWindowRepository) {
    suspend operator fun invoke(window: FastingWindow) = fastingWindowRepository.save(window)
}
