package com.englishspeakinghub.freeoffline.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.englishspeakinghub.freeoffline.Helpers.Room
import com.englishspeakinghub.freeoffline.Screens.Meeting
import com.englishspeakinghub.freeoffline.databinding.AdUnifiedBinding
import com.englishspeakinghub.freeoffline.databinding.MeetingsItemBinding
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.nativead.NativeAdOptions

class RoomAdapter (private val context: Context, private val list  :ArrayList<Room>):RecyclerView.Adapter<RoomAdapter.holder>() {
    class holder(val binding: MeetingsItemBinding) : RecyclerView.ViewHolder(binding.root){
        val view = binding
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): holder {
        return  holder(MeetingsItemBinding.inflate(LayoutInflater.from(context) , parent,false))
    }

    override fun onBindViewHolder(holder: holder, position: Int) {
        holder.view.roomHost.text = "Host : " + list[position].host
        holder.view.roomTitle.text = list[position].roomId
        holder.view.roomLimit.text = "Limit: "+list[position].maxParticipant
        holder.view.currentUsers.text = list[position].enrolledUsers
        holder.view.btnJoin.setOnClickListener {
            val intent = Intent(context,Meeting::class.java)
            intent.putExtra("room",list[position].roomId)
            context.startActivity(intent)
        }
        holder.view.btnShareRoom.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            val roomLink = "Join To My Meeting using this Room id  - ${list[position].roomId} - " +
                    " On The Most Trusted Video Conferencing App"
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, roomLink)
            context.startActivity(Intent.createChooser(shareIntent, "Share Room With"))
        }
        if (position%4 == 0){
            val adLoader = AdLoader.Builder(context,"ca-app-pub-3940256099942544/2247696110")
                .forNativeAd { nativeAd:com.google.android.gms.ads.nativead.NativeAd->
                    holder.view.ads.visibility =View.VISIBLE
                    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val viewUnifiedBinding = AdUnifiedBinding.inflate(layoutInflater)
                    populateNativeAdView(nativeAd, unifiedAdBinding = viewUnifiedBinding)
                    holder.view.ads.removeAllViews()
                    holder.view.ads.addView(viewUnifiedBinding.root)
                }
                .withAdListener(object:com.google.android.gms.ads.AdListener(){
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .build()
                )
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }
    private fun populateNativeAdView(nativeAd: com.google.android.gms.ads.nativead.NativeAd, unifiedAdBinding: AdUnifiedBinding) {
        val nativeAdView = unifiedAdBinding.root

        // Set the media view.
        nativeAdView.mediaView = unifiedAdBinding.adMedia

        // Set other ad assets.
        nativeAdView.headlineView = unifiedAdBinding.adHeadline
        nativeAdView.bodyView = unifiedAdBinding.adBody
        nativeAdView.callToActionView = unifiedAdBinding.adCallToAction
        nativeAdView.iconView = unifiedAdBinding.adAppIcon
        nativeAdView.priceView = unifiedAdBinding.adPrice
        nativeAdView.starRatingView = unifiedAdBinding.adStars
        nativeAdView.storeView = unifiedAdBinding.adStore
        nativeAdView.advertiserView = unifiedAdBinding.adAdvertiser

        // The headline and media content are guaranteed to be in every NativeAd.

        unifiedAdBinding.adMedia.visibility= View.GONE

        unifiedAdBinding.adHeadline.text = nativeAd.headline
        nativeAd.mediaContent?.let { unifiedAdBinding.adMedia.setMediaContent(it) }

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.

        if (nativeAd.body == null) {
            unifiedAdBinding.adBody.visibility = View.GONE
        } else {
            unifiedAdBinding.adBody.visibility = View.GONE
            unifiedAdBinding.adBody.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            unifiedAdBinding.adCallToAction.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adCallToAction.visibility = View.VISIBLE
            unifiedAdBinding.adCallToAction.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            unifiedAdBinding.adAppIcon.visibility = View.GONE
        } else {
            unifiedAdBinding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
            unifiedAdBinding.adAppIcon.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            unifiedAdBinding.adPrice.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adPrice.visibility = View.VISIBLE
            unifiedAdBinding.adPrice.text = nativeAd.price
        }

        if (nativeAd.store == null) {
            unifiedAdBinding.adStore.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adStore.visibility = View.VISIBLE
            unifiedAdBinding.adStore.text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            unifiedAdBinding.adStars.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adStars.rating = nativeAd.starRating!!.toFloat()
            unifiedAdBinding.adStars.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            unifiedAdBinding.adAdvertiser.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adAdvertiser.text = nativeAd.advertiser
            unifiedAdBinding.adAdvertiser.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        nativeAdView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc = nativeAd.mediaContent?.videoController

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc != null && vc.hasVideoContent()) {

            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {
                    override fun onVideoEnd() {
                        // Publishers should allow native ads to complete video playback before
                        // refreshing or replacing them with another ad in the same UI location.

                        super.onVideoEnd()
                    }
                }
        } else {

        }
    }
}