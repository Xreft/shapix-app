package com.xreft.shapix

import android.app.Activity
import com.adivery.sdk.Adivery

object Ads {
    const val APP_ID = "f8a29e2b-d8a6-4a39-a115-4609999d92b4"
    const val BANNER_ID = "d814d424-634e-45b8-87eb-1f7e53584427"
    const val INTERSTITIAL_ID = "f09630be-ad40-43d9-9d37-76a83e8a75d2"

    fun init(activity: Activity) {
        runCatching {
            Adivery.configure(activity.application, APP_ID)
            Adivery.setLoggingEnabled(true)
            Adivery.prepareInterstitialAd(activity, INTERSTITIAL_ID)
        }
    }

    fun showInterstitialIfReady() {
        runCatching {
            if (Adivery.isLoaded(INTERSTITIAL_ID)) Adivery.showAd(INTERSTITIAL_ID)
        }
    }
}
