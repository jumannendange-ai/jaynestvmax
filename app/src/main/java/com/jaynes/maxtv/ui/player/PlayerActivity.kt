package com.jaynes.maxtv.ui.player

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import com.jaynes.maxtv.databinding.ActivityPlayerBinding

@UnstableApi
class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHANNEL_NAME = "channel_name"
        const val EXTRA_STREAM_URL   = "stream_url"
        const val EXTRA_DRM_KEY      = "drm_key"
        const val EXTRA_STREAM_TYPE  = "stream_type"
        const val EXTRA_LOGO_URL     = "logo_url"

        fun start(context: Context, channelName: String, streamUrl: String,
                  drmKey: String? = null, streamType: String = "hls", logoUrl: String? = null) {
            context.startActivity(Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_CHANNEL_NAME, channelName)
                putExtra(EXTRA_STREAM_URL, streamUrl)
                putExtra(EXTRA_DRM_KEY, drmKey)
                putExtra(EXTRA_STREAM_TYPE, streamType)
                putExtra(EXTRA_LOGO_URL, logoUrl)
            })
        }
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null

    private val channelName by lazy { intent.getStringExtra(EXTRA_CHANNEL_NAME) ?: "Live TV" }
    private val streamUrl   by lazy { intent.getStringExtra(EXTRA_STREAM_URL) ?: "" }
    private val drmKey      by lazy { intent.getStringExtra(EXTRA_DRM_KEY) }
    private val streamType  by lazy { intent.getStringExtra(EXTRA_STREAM_TYPE) ?: "hls" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setupUI()
        initPlayer()
    }

    private fun setupUI() {
        binding.tvChannelName.text = channelName

        // Back button
        binding.ivBack.setOnClickListener { finish() }

        // PiP button
        binding.ivPip.setOnClickListener { enterPiP() }

        // Lock screen
        var locked = false
        binding.ivLock.setOnClickListener {
            locked = !locked
            binding.playerControls.visibility = if (locked) View.GONE else View.VISIBLE
            binding.ivLock.setImageResource(
                if (locked) android.R.drawable.ic_lock_lock
                else android.R.drawable.ic_lock_idle_lock
            )
        }

        // Quality selector
        setupQualitySelector()
    }

    private fun setupQualitySelector() {
        val qualities = listOf("AUTO", "1080p", "720p", "480p", "360p")
        binding.rvQuality.adapter = QualityAdapter(qualities) { quality ->
            // ExoPlayer track selection
            if (quality == "AUTO") {
                player?.trackSelectionParameters = player?.trackSelectionParameters
                    ?.buildUpon()
                    ?.setMaxVideoSizeSd()
                    ?.clearVideoSizeConstraints()
                    ?.build() ?: return@QualityAdapter
            }
        }
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        val mediaSource = buildMediaSource(streamUrl, drmKey)
        player?.apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> showBuffering(true)
                        Player.STATE_READY     -> showBuffering(false)
                        Player.STATE_ENDED     -> finish()
                        Player.STATE_IDLE      -> {}
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    showError(error.message ?: "Hitilafu ya stream")
                }
            })
        }
    }

    private fun buildMediaSource(url: String, clearKey: String?): MediaSource {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(
                mapOf("User-Agent" to "JaynesMaxTV/1.0 (Android)")
            )

        return if (streamType == "dash" || url.endsWith(".mpd")) {
            val mediaItemBuilder = MediaItem.Builder().setUri(url)

            // ClearKey DRM
            if (clearKey != null) {
                val parts = clearKey.split(":")
                if (parts.size == 2) {
                    val drmConfig = MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID)
                        .setLicenseUri(buildClearKeyLicense(parts[0], parts[1]))
                        .build()
                    mediaItemBuilder.setDrmConfiguration(drmConfig)
                }
            }

            DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItemBuilder.build())
        } else {
            // HLS
            val mediaItem = MediaItem.Builder()
                .setUri(url)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build()
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }
    }

    private fun buildClearKeyLicense(keyId: String, key: String): String {
        // ClearKey license URL format
        return "data:application/json,{\"keys\":[{\"kty\":\"oct\",\"k\":\"${
            android.util.Base64.encodeToString(
                hexToBytes(key), android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING
            )
        }\",\"kid\":\"${
            android.util.Base64.encodeToString(
                hexToBytes(keyId), android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING
            )
        }\"}],\"type\":\"temporary\"}"
    }

    private fun hexToBytes(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in result.indices) {
            result[i] = hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return result
    }

    private fun showBuffering(show: Boolean) {
        binding.progressBuffering.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        binding.btnRetry.visibility = View.VISIBLE
        binding.btnRetry.setOnClickListener {
            binding.tvError.visibility = View.GONE
            binding.btnRetry.visibility = View.GONE
            initPlayer()
        }
    }

    private fun enterPiP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(isInPiPMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPiPMode, newConfig)
        binding.playerControls.visibility = if (isInPiPMode) View.GONE else View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        if (!isInPictureInPictureMode) player?.pause()
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
