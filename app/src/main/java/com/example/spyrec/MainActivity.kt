package com.example.spyrec

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.spyrec.databinding.ActivityMainBinding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


const val REQUEST_CODE = 200

class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener {
    private val TAG = "MainActivity"
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false
    private lateinit var binding: ActivityMainBinding
    private var dirPath = ""
    private var fileName = ""
    private var isRecording = false
    private var isPaused = false

    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator

    private lateinit var recorder: MediaRecorder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)

        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        binding.btnRecord.setOnClickListener {
            when {
                isPaused -> resumeRecorder()
                isRecording -> pauseRecorder()
                else -> startRecording()
            }
//            println("Build.VERSION.SDK_INT ${Build.VERSION.SDK_INT}")
//            println("Build.VERSION_CODES.O ${Build.VERSION_CODES.O}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(200)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun pauseRecorder() {
        recorder.pause()
        isPaused = true
        binding.btnRecord.setImageResource(R.drawable.ic_record)

        timer.pause()
    }

    private fun resumeRecorder() {
        recorder.resume()
        isPaused = false
        binding.btnRecord.setImageResource(R.drawable.ic_pause)

        timer.start()
    }

    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"

        var simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        var date = simpleDateFormat.format(Date())
        fileName = "audio_record_$date"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$fileName.mp3")
            try {
                prepare()
            } catch (_: IOException) {
                Log.d(TAG, "Error")
            }
            start()
        }

        binding.btnRecord.setImageResource(R.drawable.ic_pause)
        isRecording = true
        isPaused = false

        timer.start()
    }

    private fun stopRecorder() {
        timer.stop()
    }

    override fun onTimerTick(duration: String) {
        binding.tvTimer.text = duration
    }
}