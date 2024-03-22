package com.example.spyrec

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.spyrec.databinding.ActivityAudioPlayerBinding

class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var binding: ActivityAudioPlayerBinding
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private var delay = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var filePath = intent.getStringExtra("filepath")
        var fileName = intent.getStringExtra("filename")

        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(filePath)
            prepare()
        }

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            binding.seekBar.progress = mediaPlayer.currentPosition
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
    }

    private fun playPausePlayer() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            binding.btnPlay.background =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_pause_circle, theme)
            handler.postDelayed(runnable, 0)
        } else {
            mediaPlayer.pause()
            binding.btnPlay.background =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)
            handler.removeCallbacks(runnable)
        }
    }
}