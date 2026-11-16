package com.phonecleaner.aicleaner.core.di

import com.phonecleaner.aicleaner.core.datastore.AppDataStore
import com.phonecleaner.aicleaner.data.apps.AppsRepository
import com.phonecleaner.aicleaner.data.media.DuplicateScanner
import com.phonecleaner.aicleaner.data.image.PhotoCompressor
import com.phonecleaner.aicleaner.data.ai.AiCleanupScanner
import com.phonecleaner.aicleaner.data.battery.BatteryRepository
import com.phonecleaner.aicleaner.data.device.DeviceInfoRepository
import com.phonecleaner.aicleaner.data.files.FilesRepository
import com.phonecleaner.aicleaner.data.whatsapp.WhatsAppScanner
import com.phonecleaner.aicleaner.data.process.ProcessRepository
import com.phonecleaner.aicleaner.data.scanner.JunkScanner
import com.phonecleaner.aicleaner.core.remote.SplashDesignProvider
import com.phonecleaner.aicleaner.core.remote.SplashDesignProviderImpl
import com.phonecleaner.aicleaner.core.utils.NetworkObserver
import com.phonecleaner.aicleaner.core.utils.NetworkObserverImpl
import com.phonecleaner.aicleaner.domain.AppsViewModel
import com.phonecleaner.aicleaner.domain.DuplicatesViewModel
import com.phonecleaner.aicleaner.domain.FeedbackViewModel
import com.phonecleaner.aicleaner.domain.HomeViewModel
import com.phonecleaner.aicleaner.domain.JunkViewModel
import com.phonecleaner.aicleaner.domain.LanguageViewModel
import com.phonecleaner.aicleaner.domain.ProcessViewModel
import com.phonecleaner.aicleaner.domain.WhatsAppViewModel
import com.phonecleaner.aicleaner.domain.PhotoCompressorViewModel
import com.phonecleaner.aicleaner.domain.AiCleanupViewModel
import com.phonecleaner.aicleaner.domain.BatteryViewModel
import com.phonecleaner.aicleaner.domain.FileCategoryViewModel
import com.phonecleaner.aicleaner.domain.FilesViewModel
import com.phonecleaner.aicleaner.domain.StorageBrowserViewModel
import com.phonecleaner.aicleaner.domain.SettingViewModel
import com.phonecleaner.aicleaner.domain.viewmodel.BannerAdViewModel
import com.phonecleaner.aicleaner.domain.viewmodel.LanguageAdsViewModel
import com.phonecleaner.aicleaner.domain.viewmodel.PremiumViewModel
import com.phonecleaner.aicleaner.domain.viewmodel.SharedNativeAdVM
import com.phonecleaner.aicleaner.domain.viewmodel.SplashVM
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single<AppDataStore> { AppDataStore(androidContext()) }
    single { JunkScanner(androidContext()) }
    single { AppsRepository(androidContext()) }
    single { ProcessRepository(androidContext()) }
    single { DuplicateScanner(androidContext()) }
    single { WhatsAppScanner(androidContext()) }
    single { PhotoCompressor(androidContext()) }
    single { FilesRepository(androidContext()) }
    single { BatteryRepository(androidContext()) }
    single { DeviceInfoRepository(androidContext()) }
    single { AiCleanupScanner(androidContext(), get()) }
}

val viewModelModule = module {
    viewModel { SharedNativeAdVM() }
    viewModel { SplashVM(androidApplication(), get()) }
    viewModel { PremiumViewModel(androidApplication()) }
    viewModel { SettingViewModel(get(), androidContext()) }
    viewModel { LanguageViewModel(get(), androidContext()) }
    viewModel { BannerAdViewModel() }
    viewModel { LanguageAdsViewModel() }
    viewModel { FeedbackViewModel() }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { JunkViewModel(get()) }
    viewModel { AppsViewModel(get()) }
    viewModel { ProcessViewModel(get()) }
    viewModel { DuplicatesViewModel(get()) }
    viewModel { WhatsAppViewModel(get()) }
    viewModel { PhotoCompressorViewModel(get()) }
    viewModel { FilesViewModel(get(), get()) }
    viewModel { FileCategoryViewModel(get(), get()) }
    viewModel { StorageBrowserViewModel(get(), get()) }
    viewModel { BatteryViewModel(get()) }
    viewModel { AiCleanupViewModel(get(), get()) }
}

val utilsModule = module {
    single<NetworkObserver> { NetworkObserverImpl(androidContext()) }
    single<SplashDesignProvider> { SplashDesignProviderImpl() }
}

val appModules = listOf(
    databaseModule, viewModelModule, utilsModule
)
