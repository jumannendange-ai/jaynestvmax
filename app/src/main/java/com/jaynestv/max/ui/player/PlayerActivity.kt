package com.jaynestv.max.ui.player

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.jaynestv.max.databinding.ActivityPlayerBinding

@UnstableApi
class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHANNEL_NAME  = "channel_name"
        const val EXTRA_STREAM_URL    = "stream_url"
        const val EXTRA_STREAM_MPD    = "stream_mpd"
        const val EXTRA_IS_LIVE       = "is_live"
        const val EXTRA_CLEARKEY_KID  = "clearkey_kid"
        const val EXTRA_CLEARKEY_KEY  = "clearkey_key"
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var streamUrl    = ""
    private var streamMpd    = ""
    private var clearkeyKid  = ""
    private var clearkeyKey  = ""
    private var useMpd       = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME) ?: "JAYNES MAX TV"
        val isLive      = intent.getBooleanExtra(EXTRA_IS_LIVE, true)
        streamUrl      = intent.getStringExtra(EXTRA_STREAM_URL)   ?: ""
        streamMpd      = intent.getStringExtra(EXTRA_STREAM_MPD)   ?: ""
        clearkeyKid    = intent.getStringExtra(EXTRA_CLEARKEY_KID) ?: ""
        clearkeyKey    = intent.getStringExtra(EXTRA_CLEARKEY_KEY) ?: ""

        binding.txtChannelName.text = channelName
        binding.badgeLive.visibility = if (isLive) View.VISIBLE else View.GONE

        // Toggle MPD/HLS button
        if (streamMpd.isNotEmpty() && streamUrl.isNotEmpty()) {
            binding.btnToggleStream.visibility = View.VISIBLE
            binding.btnToggleStream.text = "MPD"
            binding.btnToggleStream.setOnClickListener { toggleStream() }
        }

        binding.btnBack.setOnClickListener { finish() }

        initPlayer()
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        // Tumia MPD kama ipo, kisha HLS
        useMpd = streamMpd.isNotEmpty()
        val url = if (useMpd) streamMpd else streamUrl

        if (url.isEmpty()) {
            showError("Hakuna stream URL")
            return
        }

        playStream(url)
    }

    private fun playStream(url: String) {
        showLoading(true)
        hideError()

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("JaynesMaxTV/4.0 Android ExoPlayer")
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(20_000)

        val mediaSource: MediaSource = when {
            // MPD / DASH
            url.endsWith(".mpd", ignoreCase = true) ||
            url.contains("manifest", ignoreCase = true) && !url.contains(".m3u8") ->
                DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(url))

            // HLS
            url.endsWith(".m3u8", ignoreCase = true) ||
            url.contains(".m3u8", ignoreCase = true) ->
                HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(url))

            // Progressive (MP4, etc.)
            else ->
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(url))
        }

        player?.apply {
            setMediaSource(mediaSource)
            playWhenReady = true
            prepare()
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> showLoading(true)
                        Player.STATE_READY     -> showLoading(false)
                        Player.STATE_ENDED     -> showLoading(false)
                        Player.STATE_IDLE      -> {}
                    }
                }
                override fun onPlayerError(error: PlaybackException) {
                    showLoading(false)
                    // Jaribu fallback — kama ilikuwa MPD jaribu HLS
                    if (useMpd && streamUrl.isNotEmpty()) {
                        useMpd = false
                        binding.btnToggleStream.text = "HLS"
                        playStream(streamUrl)
                    } else {
                        showError("Stream haipatikani. Jaribu tena.")
                    }
                }
            })
        }
    }

    private fun toggleStream() {
        useMpd = !useMpd
        binding.btnToggleStream.text = if (useMpd) "HLS" else "MPD"
        val url = if (useMpd) streamMpd else streamUrl
        if (url.isNotEmpty()) playStream(url)
    }

    private fun showLoading(show: Boolean) {
        binding.loadingBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(msg: String) {
        binding.errorLayout.visibility = View.VISIBLE
        binding.txtError.text = msg
        binding.btnRetry.setOnClickListener {
            hideError()
            playStream(if (useMpd) streamMpd else streamUrl)
        }
    }

    private fun hideError() {
        binding.errorLayout.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
