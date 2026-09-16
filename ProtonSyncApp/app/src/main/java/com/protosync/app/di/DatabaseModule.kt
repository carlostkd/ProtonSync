package com.protosync.app.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.auth.data.db.AuthDatabase
import me.proton.core.challenge.data.db.ChallengeDatabase
import me.proton.core.contact.data.local.db.ContactDatabase
import me.proton.core.eventmanager.data.db.EventMetadataDatabase
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.humanverification.data.db.HumanVerificationDatabase
import me.proton.core.key.data.db.KeySaltDatabase
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.label.data.local.LabelDatabase
import me.proton.core.mailsettings.data.db.MailSettingsDatabase
import me.proton.core.notification.data.local.db.NotificationDatabase
import me.proton.core.observability.data.db.ObservabilityDatabase
import me.proton.core.payment.data.local.db.PaymentDatabase
import me.proton.core.push.data.local.db.PushDatabase
import me.proton.core.telemetry.data.db.TelemetryDatabase
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.db.AddressKeyDatabase
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.db.UserKeyDatabase
import me.proton.core.usersettings.data.db.OrganizationDatabase
import me.proton.core.usersettings.data.db.UserSettingsDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppDatabaseProviderModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): com.protosync.app.db.AppDatabase =
        com.protosync.app.db.AppDatabase.buildDatabase(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppDatabaseBindsModule {
    @Binds
    abstract fun provideAccountDatabase(appDatabase: com.protosync.app.db.AppDatabase): AccountDatabase

    @Binds
    abstract fun provideUserDatabase(appDatabase: com.protosync.app.db.AppDatabase): UserDatabase

    @Binds
    abstract fun provideAddressDatabase(appDatabase: com.protosync.app.db.AppDatabase): AddressDatabase

    @Binds
    abstract fun provideAddressKeyDatabase(appDatabase: com.protosync.app.db.AppDatabase): AddressKeyDatabase

    @Binds
    abstract fun provideUserKeyDatabase(appDatabase: com.protosync.app.db.AppDatabase): UserKeyDatabase

    @Binds
    abstract fun provideKeySaltDatabase(appDatabase: com.protosync.app.db.AppDatabase): KeySaltDatabase

    @Binds
    abstract fun providePublicAddressDatabase(appDatabase: com.protosync.app.db.AppDatabase): PublicAddressDatabase

    @Binds
    abstract fun provideHumanVerificationDatabase(appDatabase: com.protosync.app.db.AppDatabase): HumanVerificationDatabase

    @Binds
    abstract fun provideMailSettingsDatabase(appDatabase: com.protosync.app.db.AppDatabase): MailSettingsDatabase

    @Binds
    abstract fun provideUserSettingsDatabase(appDatabase: com.protosync.app.db.AppDatabase): UserSettingsDatabase

    @Binds
    abstract fun provideOrganizationDatabase(appDatabase: com.protosync.app.db.AppDatabase): OrganizationDatabase

    @Binds
    abstract fun provideContactDatabase(appDatabase: com.protosync.app.db.AppDatabase): ContactDatabase

    @Binds
    abstract fun provideEventMetadataDatabase(appDatabase: com.protosync.app.db.AppDatabase): EventMetadataDatabase

    @Binds
    abstract fun provideLabelDatabase(appDatabase: com.protosync.app.db.AppDatabase): LabelDatabase

    @Binds
    abstract fun provideFeatureFlagDatabase(appDatabase: com.protosync.app.db.AppDatabase): FeatureFlagDatabase

    @Binds
    abstract fun provideChallengeDatabase(appDatabase: com.protosync.app.db.AppDatabase): ChallengeDatabase

    @Binds
    abstract fun provideAuthDatabase(appDatabase: com.protosync.app.db.AppDatabase): AuthDatabase

    @Binds
    abstract fun providePushDatabase(appDatabase: com.protosync.app.db.AppDatabase): PushDatabase

    @Binds
    abstract fun providePaymentDatabase(appDatabase: com.protosync.app.db.AppDatabase): PaymentDatabase

    @Binds
    abstract fun provideObservabilityDatabase(appDatabase: com.protosync.app.db.AppDatabase): ObservabilityDatabase

    @Binds
    abstract fun provideTelemetryDatabase(appDatabase: com.protosync.app.db.AppDatabase): TelemetryDatabase

    @Binds
    abstract fun provideNotificationDatabase(appDatabase: com.protosync.app.db.AppDatabase): NotificationDatabase
}
