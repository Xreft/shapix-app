package com.xreft.shapix

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.adivery.sdk.*

@Composable
fun BannerAd(placementId: String) {
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(50.dp),
        factory = { ctx ->
            AdiveryBannerAdView(ctx).apply {
                setPlacementId(placementId)
                setBannerSize(BannerSize.BANNER)
                setBannerAdListener(object : AdiveryAdListener() {
                    override fun onAdLoaded() {}
                    override fun onError(reason: String) {}
                    override fun onAdClicked() {}
                })
                loadAd()
            }
        }
    )
}
