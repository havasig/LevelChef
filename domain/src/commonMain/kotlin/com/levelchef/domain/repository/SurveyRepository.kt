package com.levelchef.domain.repository

import com.levelchef.core.model.SurveyResponse
import kotlinx.coroutines.flow.Flow

/** Persistence boundary for the mandatory first-launch survey. */
interface SurveyRepository {
    /** Emits the stored response, or `null` while the survey has not been completed. */
    fun observeResponse(): Flow<SurveyResponse?>

    /** Stores (or overwrites) the single survey response. */
    suspend fun save(response: SurveyResponse)
}
