package com.phonecleaner.aicleaner.core.remote

import com.phonecleaner.aicleaner.core.common.tryCatch

/** Which splash screen to show. */
enum class SplashDesign {
    Classic,
    New;

    companion object {
        /** Remote value 2 = new design; anything else (1, missing, garbage) = classic. */
        fun fromRemote(value: Int): SplashDesign = if (value == 2) New else Classic
    }
}

interface SplashDesignProvider {

    /** Splash design for the current remote state (fetched or default values). Never throws. */
    fun resolve(): SplashDesign
}

class SplashDesignProviderImpl : SplashDesignProvider {

    override fun resolve(): SplashDesign {
        var design = SplashDesign.Classic
        tryCatch {
            design = SplashDesign.fromRemote(RemoteConstants.splashScreenType)
        }
        return design
    }
}
