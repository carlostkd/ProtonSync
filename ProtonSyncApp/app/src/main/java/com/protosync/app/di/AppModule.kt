package com.protosync.app.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.compose.theme.AppTheme
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.Product
import me.proton.core.plan.domain.ProductOnlyPaidPlans
import me.proton.core.plan.domain.SupportSignupPaidPlans
import me.proton.core.plan.domain.SupportUpgradePaidPlans
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    fun provideAppStore(): AppStore = AppStore.GooglePlay

    @Provides
    @Singleton
    fun provideProduct(): Product = Product.Mail

    @Provides
    @Singleton
    fun provideRequiredAccountType(): AccountType = AccountType.Internal

    @Provides
    @Singleton
    @SupportSignupPaidPlans
    fun provideSupportSignupPaidPlans(): Boolean = true

    @Provides
    @Singleton
    @SupportUpgradePaidPlans
    fun provideSupportUpgradePaidPlans(): Boolean = true

    @Provides
    @Singleton
    @ProductOnlyPaidPlans
    fun provideProductOnlyPaidPlans(): Boolean = false
}

@Module
@InstallIn(SingletonComponent::class)
object AppThemeModule {
    @Provides
    fun provideAppTheme(): AppTheme = AppTheme { content -> ProtonTheme { content() } }
}

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
