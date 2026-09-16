package com.protosync.app.calendar

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CalendarApi : BaseRetrofitApi {
    @GET("calendar/v1")
    suspend fun getCalendars(): CalendarListResponse

    @GET("calendar/v1/{calendarId}/events")
    suspend fun getEvents(
        @Path("calendarId") calendarId: String,
        @Query("Start") start: Long,
        @Query("End") end: Long,
        @Query("Timezone") timezone: String = "UTC",
        @Query("Page") page: Int = 0,
        @Query("PageSize") pageSize: Int = 100
    ): CalendarEventsResponse

    @GET("calendar/v1/{calendarId}/keys")
    suspend fun getCalendarKeys(@Path("calendarId") calendarId: String): CalendarKeysResponse

    @GET("calendar/v1/{calendarId}/members")
    suspend fun getCalendarMembers(@Path("calendarId") calendarId: String): CalendarMembersResponse

    @GET("calendar/v1/{calendarId}/passphrases")
    suspend fun getCalendarPassphrase(@Path("calendarId") calendarId: String): CalendarPassphraseResponse
}