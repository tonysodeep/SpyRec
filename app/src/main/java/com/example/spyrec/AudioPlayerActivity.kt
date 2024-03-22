package com.example.spyrec

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.spyrec.databinding.ActivityAudioPlayerBinding

class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAudioPlayerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}