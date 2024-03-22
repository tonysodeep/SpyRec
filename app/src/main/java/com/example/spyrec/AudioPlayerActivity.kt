package com.example.spyrec

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.spyrec.databinding.ActivityAudioPlayerBinding
import java.text.DecimalFormat
import java.text.NumberFormat

class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var binding: ActivityAudioPlayerBinding
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler

    private var delay = 1000L
    private var jumpValue = 1000
    private var playbackSpeed = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var filePath = intent.getStringExtra("filepath")
        var fileName = intent.getStringExtra("filename")

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.tvFilename.text = fileName

        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(filePath)
            prepare()
        }

        binding.tvTrackDuration.text = dateFormat(mediaPlayer.duration)

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            binding.seekBar.progress = mediaPlayer.currentPosition
            binding.tvTrackProgress.text = dateFormat(mediaPlayer.currentPosition)
            handler.postDelayed(runnable, delay)
        }

        binding.btnPlay.setOnClickListener {
            playPausePlayer()
        }

        playPausePlayer()
        binding.seekBar.max = mediaPlayer.duration

        mediaPlayer.setOnCompletionListener {
            binding.btnPlay.background =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)
            handler.removeCallbacks(runnable)
        }

        binding.btnForward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition + jumpValue)
            binding.seekBar.progress += jumpValue
        }
        binding.btnBackward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition - jumpValue)
            binding.seekBar.progress -= jumpValue
        }

        binding.chip.setOnClickListener {
            if (playbackSpeed != 2f)
                playbackSpeed += 0.5f
            else
                playbackSpeed = 0.5f
            mediaPlayer.playbackParams = PlaybackParams().setSpeed(playbackSpeed)
            binding.chip.text = "x $playbackSpeed"
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    mediaPlayer.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }

    private fun playPausePlayer() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            binding.btnPlay.background =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_pause_circle, theme)
            handler.postDelayed(runnable, delay)
        } else {
            mediaPlayer.pause()
            binding.btnPlay.background =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)
            handler.removeCallbacks(runnable)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer.stop()
        mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }

    private fun dateFormat(duration: Int): String {
        var d = duration / 1000
        var s = d % 60
        var m = (d / 60 % 60)
        var h = ((d - m * 60) / 360).toInt()
        val f: NumberFormat = DecimalFormat("00")
        var str = "$m:${f.format(s)}"

        if (h > 0) {
            str = "$h:$str"
        }
        return str
    }
}