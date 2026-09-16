package com.protosync.app.calendar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalendarListResponse(
    @SerialName("Calendars") val calendars: List<CalendarInfo>
)

@Serializable
data class CalendarInfo(
    @SerialName("ID") val id: String,
    @SerialName("Name") val name: String = "",
    @SerialName("Description") val description: String = "",
    @SerialName("Color") val color: String = "#4285F4",
    @SerialName("Display") val display: Int = 0,
    @SerialName("Members") val members: List<CalendarMemberInfo> = emptyList()
) {
    val displayName: String
        get() = if (name.isNotBlank()) name else members.firstOrNull { it.name.isNotBlank() }?.name.orEmpty()

    val displayColor: String
        get() = if (color.isNotBlank()) color else members.firstOrNull { it.color.isNotBlank() }?.color.orEmpty()

    val isDisplayed: Boolean
        get() = display != 0 || members.any { it.display != 0 }
}

@Serializable
data class CalendarMemberInfo(
    @SerialName("ID") val id: String,
    @SerialName("Name") val name: String = "",
    @SerialName("Color") val color: String = "",
    @SerialName("Display") val display: Int = 0,
    @SerialName("Flags") val flags: Int = 0,
    @SerialName("Permissions") val permissions: Int = 0,
    @SerialName("Email") val email: String = "",
    @SerialName("AddressID") val addressId: String = "",
    @SerialName("CalendarID") val calendarId: String = ""
)

@Serializable
data class CalendarKeysResponse(
    @SerialName("Keys") val keys: List<CalendarKey>
)

@Serializable
data class CalendarKey(
    @SerialName("ID") val id: String,
    @SerialName("PrivateKey") val privateKey: String,
    @SerialName("Fingerprint") val fingerprint: String = "",
    @SerialName("Flags") val flags: Int = 0
)

@Serializable
data class CalendarMembersResponse(
    @SerialName("Members") val members: List<CalendarMember>
)

@Serializable
data class CalendarMember(
    @SerialName("ID") val id: String,
    @SerialName("PrivateKey") val privateKey: String,
    @SerialName("EmailAddressID") val emailAddressId: String = "",
    @SerialName("Flags") val flags: Int = 0
)

@Serializable
data class CalendarPassphraseResponse(
    @SerialName("Code") val code: Int,
    @SerialName("Passphrases") val passphrases: List<CalendarPassphraseEntry>
)

@Serializable
data class CalendarPassphraseEntry(
    @SerialName("ID") val id: String,
    @SerialName("Flags") val flags: Int,
    @SerialName("CalendarID") val calendarId: String,
    @SerialName("MemberPassphrases") val memberPassphrases: List<MemberPassphraseEntry>
)

@Serializable
data class MemberPassphraseEntry(
    @SerialName("MemberID") val memberId: String,
    @SerialName("Passphrase") val passphrase: String,
    @SerialName("Signature") val signature: String = ""
)

@Serializable
data class MailboxCalendarKeysResponse(
    @SerialName("AddressKeys") val addressKeys: List<MailboxAddressKey>
)

@Serializable
data class MailboxAddressKey(
    @SerialName("PrivateKey") val privateKey: String,
    @SerialName("Fingerprint") val fingerprint: String = "",
    @SerialName("Flags") val flags: Int = 0,
    @SerialName("Primary") val primary: Int = 0
)

@Serializable
data class CalendarEventsResponse(
    @SerialName("Events") val events: List<CalendarEventSyncData>,
    @SerialName("Total") val total: Int = 0
)

@Serializable
data class CalendarEventSyncData(
    @SerialName("CalendarID") val calendarId: String,
    @SerialName("ID") val id: String,
    @SerialName("UID") val uid: String? = null,
    @SerialName("SharedKeyPacket") val sharedKeyPacket: String? = null,
    @SerialName("SharedSecret") val sharedSecret: String? = null,
    @SerialName("CalendarKeyPacket") val calendarKeyPacket: String? = null,
    @SerialName("CalendarEvent") val calendarEvent: String? = null, // base64 encrypted+gzip+protobuf
    @SerialName("SharedEvents") val sharedEvents: List<SharedEventData> = emptyList(),
    @SerialName("CalendarEvents") val calendarEvents: List<SharedEventData> = emptyList(),
    @SerialName("StartTime") val startTime: Long = 0,
    @SerialName("EndTime") val endTime: Long = 0,
    @SerialName("StartTimezone") val startTimezone: String? = null,
    @SerialName("EndTimezone") val endTimezone: String? = null,
    @SerialName("FullDay") val fullDay: Int = 0,
    @SerialName("UpdateTime") val updateTime: Long = 0,
    @SerialName("Attendees") val attendees: List<EventAttendee> = emptyList(),
    @SerialName("Notifications") val notifications: List<EventNotification>? = null,
    @SerialName("RecurrenceID") val recurrenceId: String? = null,
    @SerialName("IsDeleted") val isDeleted: Boolean = false,
    @SerialName("IsProtonProton") val isProtonProton: Boolean = false
) {
    val clearTextICal: String?
        get() = sharedEvents.firstOrNull { it.type == 2 }?.data
            ?: calendarEvents.firstOrNull { it.type == 2 }?.data

    val encryptedICalData: String?
        get() = sharedEvents.firstOrNull { it.type == 3 }?.data
            ?: calendarEvents.firstOrNull { it.type == 3 }?.data

    val sharedKeyPacketForDecrypt: String?
        get() = sharedEvents.firstOrNull { it.type == 3 }?.keyPacket ?: sharedKeyPacket
}

@Serializable
data class SharedEventData(
    @SerialName("Type") val type: Int = 0,
    @SerialName("Data") val data: String = "",
    @SerialName("Signature") val signature: String? = null,
    @SerialName("Author") val author: String? = null,
    @SerialName("KeyPacket") val keyPacket: String? = null,
    @SerialName("EncryptedSessionKey") val encryptedSessionKey: String? = null,
    @SerialName("SignatureKeyPacket") val signatureKeyPacket: String? = null,
    @SerialName("MIMEType") val mimeType: String? = null
)

@Serializable
data class EventAttendee(
    @SerialName("ID") val id: String = "",
    @SerialName("Token") val token: String = "",
    @SerialName("Status") val status: Int = 0,
    @SerialName("DisplayName") val displayName: String = "",
    @SerialName("Email") val email: String = ""
)

@Serializable
data class EventNotification(
    @SerialName("ID") val id: String = "",
    @SerialName("Type") val type: Int = 0,
    @SerialName("Minutes") val minutes: Int = 0
)

// Session-level data needed for event decryption
data class CalendarEventSessionData(
    val calendarSessionKey: SessionKey? = null,
    val decryptedEventSessionKey: SessionKey? = null
)

typealias SessionKey = me.proton.core.crypto.common.pgp.SessionKey