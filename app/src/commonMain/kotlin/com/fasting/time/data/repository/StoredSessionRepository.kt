package com.fasting.time.data.repository

import com.fasting.time.data.session.SessionLogStore
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.Session
import com.fasting.time.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Instant

/**
 * Keeps the log as one line of text: sessions separated by semicolons, each one a phase name, a
 * start and an end in milliseconds since the epoch, separated by commas. A few sessions a day
 * stay small enough to rewrite whole on every change.
 */
class StoredSessionRepository(private val store: SessionLogStore) : SessionRepository {
    // Read once, on first use; after that the store is only written to.
    private val sessions by lazy { MutableStateFlow(store.read().orEmpty().toSessions()) }

    override fun observe(): Flow<List<Session>> = sessions

    override suspend fun add(session: Session) {
        sessions.value += session
        store.write(sessions.value.joinToString(SessionSeparator) { it.toText() })
    }

    private fun Session.toText(): String =
        listOf(phase.name, startedAt.toEpochMilliseconds(), endedAt.toEpochMilliseconds())
            .joinToString(FieldSeparator)

    /** Skips whatever this version can't read, rather than losing the rest of the log with it. */
    private fun String.toSessions(): List<Session> =
        split(SessionSeparator).mapNotNull { text ->
            val fields = text.split(FieldSeparator)
            val phase = FastingPhase.entries.firstOrNull { it.name == fields[0] }
            val startedAt = fields.getOrNull(1)?.toLongOrNull()
            val endedAt = fields.getOrNull(2)?.toLongOrNull()
            if (phase == null || startedAt == null || endedAt == null) {
                null
            } else {
                Session(
                    phase,
                    Instant.fromEpochMilliseconds(startedAt),
                    Instant.fromEpochMilliseconds(endedAt),
                )
            }
        }

    private companion object {
        const val SessionSeparator = ";"
        const val FieldSeparator = ","
    }
}
