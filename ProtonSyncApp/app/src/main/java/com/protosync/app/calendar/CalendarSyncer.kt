package com.protosync.app.calendar

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import com.protosync.app.util.SyncLog as Log
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.domain.entity.UserId
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import com.protosync.app.util.SyncAccount

@Singleton
class CalendarSyncer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncAccount: SyncAccount
) {

    private val TAG = "CalendarSyncer"

    suspend fun sync(userId: UserId, session: CalendarSession, calendarApi: CalendarApi) {
        syncAccount.ensureExists(ACCOUNT_TYPE, ACCOUNT_NAME)
        val resolver = context.contentResolver
        Log.i(TAG, "Fetching calendar list...")
        val calendarList = calendarApi.getCalendars().calendars.filter { it.isDisplayed }
        Log.i(TAG, "Calendar list: ${calendarList.map { "${it.displayName} (${it.id})" }}")

        val startEpoch = 1577836800L  // 2020-01-01 UTC
        val endEpoch = 4102444800L    // 2100-01-01 UTC

        for (calendarInfo in calendarList) {
            val calId = getOrCreateAndroidCalendar(resolver, calendarInfo)
            Log.i(TAG, "Android calendar ready: id=$calId name=${calendarInfo.displayName}")

            val events = mutableListOf<CalendarEventSyncData>()
            var page = 0
            do {
                Log.i(TAG, "Fetching events page $page for ${calendarInfo.id}...")
                val response = calendarApi.getEvents(
                    calendarId = calendarInfo.id,
                    start = startEpoch,
                    end = endEpoch,
                    page = page
                )
                Log.i(TAG, "Events page $page: ${response.events.size}, total: ${response.total}")
                if (response.events.isEmpty()) break
                events += response.events
                if (events.size >= response.total) break
                page++
            } while (true)

            // Collect events that have encrypted content (Type 3) to batch-decrypt
            val eventsToDecrypt = events.filter {
                it.encryptedICalData != null && !it.isDeleted
            }
            Log.i(TAG, "Decrypting ${eventsToDecrypt.size} events for ${calendarInfo.displayName}...")
            val decryptedMap = session.decryptEvents(calendarInfo.id, eventsToDecrypt)
            Log.i(TAG, "Decrypted ${decryptedMap.size}/${eventsToDecrypt.size} events")

            var written = 0
            for (eventData in events) {
                if (eventData.isDeleted) {
                    deleteEventIfExists(resolver, eventData.uid ?: eventData.id)
                    continue
                }
                val decrypted = decryptedMap[eventData.id]
                if (upsertEvent(resolver, calId, eventData, decrypted)) written++
            }
            Log.i(TAG, "Wrote $written/${events.size} events for ${calendarInfo.displayName}")
        }
    }

    private fun getOrCreateAndroidCalendar(resolver: ContentResolver, info: CalendarInfo): Long {
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
        val selection = "${CalendarContract.Calendars.ACCOUNT_NAME} = ? AND ${CalendarContract.Calendars.ACCOUNT_TYPE} = ?"
        val selectionArgs = arrayOf(ACCOUNT_NAME, ACCOUNT_TYPE)

        resolver.query(CalendarContract.Calendars.CONTENT_URI, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) return cursor.getLong(0)
        }

        val batch = ArrayList<ContentProviderOperation>()
        val calUri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
            .build()

        val colorInt = try { android.graphics.Color.parseColor(info.displayColor) } catch (e: Exception) { 0x4285F4.toInt() }

        batch += ContentProviderOperation.newInsert(calUri)
            .withValue(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            .withValue(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
            .withValue(CalendarContract.Calendars.NAME, info.id)
            .withValue(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, info.displayName.ifEmpty { "Proton" })
            .withValue(CalendarContract.Calendars.CALENDAR_COLOR, colorInt)
            .withValue(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
            .withValue(CalendarContract.Calendars.VISIBLE, 1)
            .withValue(CalendarContract.Calendars.SYNC_EVENTS, 1)
            .withValue(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().id)
            .build()

        val results = resolver.applyBatch("com.android.calendar", batch)
        return ContentUris.parseId(results[0].uri!!)
    }

    private fun upsertEvent(
        resolver: ContentResolver,
        calId: Long,
        eventData: CalendarEventSyncData,
        decryptedContent: String?
    ): Boolean {
        // Determine title and description from decrypted content or clearText iCal
        val (title, description) = extractTitleAndDescription(eventData, decryptedContent)

        val startEpoch = when {
            eventData.startTime > 0 -> eventData.startTime * 1000
            else -> System.currentTimeMillis()
        }
        val endEpoch = when {
            eventData.endTime > 0 -> eventData.endTime * 1000
            eventData.fullDay != 0 -> startEpoch + 86_400_000L
            else -> startEpoch + 3_600_000L
        }

        val eventUri = CalendarContract.Events.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Events.ACCOUNT_NAME, ACCOUNT_NAME)
            .appendQueryParameter(CalendarContract.Events.ACCOUNT_TYPE, ACCOUNT_TYPE)
            .build()

        val uid = (eventData.uid ?: eventData.id)
        deleteEventIfExists(resolver, uid)

        val batch = ArrayList<ContentProviderOperation>()
        batch += ContentProviderOperation.newInsert(eventUri)
            .withValue(CalendarContract.Events.TITLE, title)
            .withValue(CalendarContract.Events.DESCRIPTION, description)
            .withValue(CalendarContract.Events.DTSTART, startEpoch)
            .withValue(CalendarContract.Events.DTEND, endEpoch)
            .withValue(CalendarContract.Events.CALENDAR_ID, calId)
            .withValue(CalendarContract.Events.ALL_DAY, if (eventData.fullDay != 0) 1 else 0)
            .withValue(CalendarContract.Events.UID_2445, uid)
            .withValue(CalendarContract.Events.EVENT_TIMEZONE, eventData.startTimezone ?: TimeZone.getDefault().id)
            .build()

        return try {
            resolver.applyBatch("com.android.calendar", batch)
            Log.i(TAG, "Inserted event ${eventData.id} ($title) at $startEpoch")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert event ${eventData.id}: ${e.message}\nuri=$eventUri", e)
            false
        }
    }

    private fun deleteEventIfExists(resolver: ContentResolver, uid: String) {
        if (uid.isEmpty()) return
        val selection = "${CalendarContract.Events.UID_2445} = ? AND ${CalendarContract.Events.ACCOUNT_NAME} = ? AND ${CalendarContract.Events.ACCOUNT_TYPE} = ?"
        val selectionArgs = arrayOf(uid, ACCOUNT_NAME, ACCOUNT_TYPE)
        resolver.delete(CalendarContract.Events.CONTENT_URI, selection, selectionArgs)
    }

    /**
     * Extract title and description from:
     *  1. Decrypted content (Type 3, from full E2E decryption) — search for JSON "summary" or VEVENT SUMMARY:
     *  2. ClearText iCal (Type 2, partial plaintext) — search for SUMMARY: line
     * Falls back to "Encrypted Event" if nothing found.
     */
    private fun extractTitleAndDescription(
        eventData: CalendarEventSyncData,
        decryptedContent: String?
    ): Pair<String, String> {
        // Try decrypted content first (most complete)
        val fromDecrypted = parseFieldsFromText(decryptedContent)
        if (fromDecrypted != null) {
            Log.d(TAG, "extractTitle: from decrypted content: '${fromDecrypted.first}'")
            return fromDecrypted
        }

        // Fallback: clearText iCal (Type 2, partial)
        val fromClearText = parseFieldsFromText(eventData.clearTextICal)
        if (fromClearText != null) {
            Log.d(TAG, "extractTitle: from clearText iCal: '${fromClearText.first}'")
            return fromClearText
        }

        Log.d(TAG, "extractTitle: no title found for ${eventData.id}, using 'Encrypted Event'")
        return "Encrypted Event" to ""
    }

    /**
     * Parse SUMMARY and DESCRIPTION from either:
     *  - A VEVENT block (iCal format): `SUMMARY:...` / `DESCRIPTION:...`
     *  - A JSON body: `"summary":"..."` / `"description":"..."`
     */
    private fun parseFieldsFromText(text: String?): Pair<String, String>? {
        if (text.isNullOrEmpty()) return null

        var summary: String? = null
        var description: String? = null

        // Try iCal SUMMARY: line
        val summaryMatch = Regex("""(?m)^SUMMARY\s*:\s*(.+)""", RegexOption.IGNORE_CASE).find(text)
        if (summaryMatch != null) {
            summary = summaryMatch.groupValues[1].trim()
                .removePrefix("\"").removeSuffix("\"")
        }

        // Try iCal DESCRIPTION: line (may be folded — continues on lines starting with space/tab)
        val descMatch = Regex(
            """(?m)^DESCRIPTION\s*:\s*((?:(?!^[A-Z]).*)+)""",
            RegexOption.IGNORE_CASE
        ).find(text)
        if (descMatch != null) {
            description = descMatch.groupValues[1]
                .replace(Regex("""(?m)^\s"""), "") // unfold continuation lines
                .trim()
                .removePrefix("\"").removeSuffix("\"")
        }

        // Try JSON "summary" / "description" (case-insensitive)
        if (summary == null) {
            val jsonSummary = Regex("""(?i)"summary"\s*:\s*"((?:[^"\\]|\\.)*)"""").find(text)
            if (jsonSummary != null) summary = jsonSummary.groupValues[1]
                .replace("\\\"", "\"").trim()
        }
        if (description == null) {
            val jsonDesc = Regex("""(?i)"description"\s*:\s*"((?:[^"\\]|\\.)*)"""").find(text)
            if (jsonDesc != null) description = jsonDesc.groupValues[1]
                .replace("\\\"", "\"").trim()
        }

        if (summary == null && description == null) return null
        return (summary ?: "Proton Event") to (description ?: "")
    }

    companion object {
        const val ACCOUNT_TYPE = "com.protosync.app.account"
        const val ACCOUNT_NAME = "ProtonSync"
    }
}
