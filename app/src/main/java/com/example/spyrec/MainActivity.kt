package com.example.spyrec

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.example.spyrec.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
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
    private var duration = ""

    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator
    private lateinit var amplitudes: ArrayList<Float>
    private lateinit var recorder: MediaRecorder
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionGranted = ActivityCompat.checkSelfPermission(
            this, permissions[0]
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords"
        ).build()

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

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
                        50, VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(200)
            }
        }

        binding.btnList.setOnClickListener {
            Toast.makeText(this, "List button", Toast.LENGTH_SHORT).show()
        }
        binding.btnDone.setOnClickListener {
            stopRecorder()
            Toast.makeText(this, "Record saved", Toast.LENGTH_SHORT).show()

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheetBG.visibility = View.VISIBLE
            binding.bottomSheet.filenameInput.setText(fileName)
        }

        binding.bottomSheet.btnCancel.setOnClickListener {
            File("$dirPath$fileName.mp3").delete()
            dismiss()
        }

        binding.bottomSheet.btnOk.setOnClickListener {
            dismiss()
            save()
        }

        binding.bottomSheetBG.setOnClickListener {
            File("$dirPath$fileName.mp3").delete()
            dismiss()
        }

        binding.btnDelete.setOnClickListener {
            stopRecorder()
            File("$dirPath$fileName.mp3")
            Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show()
        }

        binding.btnDelete.isClickable = false
    }

    private fun save() {
        val newFilename = binding.bottomSheet.filenameInput.text.toString()
        if (newFilename != fileName) {
            var newFile = File("$dirPath$newFilename.mp3")
            File("$dirPath$fileName.mp3").renameTo(newFile)
        }
        var filePath = "$dirPath$newFilename.mp3"
        var timestamp = Date().time
        var ampsPath = "$dirPath$newFilename"

        try {
            var fos = FileOutputStream(ampsPath)
            var out = ObjectOutputStream(fos)
            out.writeObject(amplitudes)
            fos.close()
            out.close()
        } catch (_: IOException) {
        }

        var record = AudioRecord(newFilename, filePath, timestamp, duration, ampsPath)
        GlobalScope.launch {
            db.audioRecordDao().insert(record)
        }
    }

    private fun dismiss() {
        binding.bottomSheetBG.visibility = View.GONE
        hideKeyboard(binding.bottomSheet.filenameInput)

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 100)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) permissionGranted =
            grantResults[0] == PackageManager.PERMISSION_GRANTED
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
        binding.btnDelete.isClickable = true
        binding.btnDelete.setImageResource(R.drawable.ic_delete)
        binding.btnList.visibility = View.GONE
        binding.btnDone.visibility = View.VISIBLE
    }

    private fun stopRecorder() {
        timer.stop()
        recorder.apply {
            stop()
            release()
        }
        isPaused = false
        isRecording = false
        binding.btnList.visibility = View.VISIBLE
        binding.btnDone.visibility = View.GONE
        binding.btnDelete.isClickable = false
        binding.btnDelete.setImageResource(R.drawable.ic_delete_disabled)
        binding.btnRecord.setImageResource(R.drawable.ic_record)
        binding.tvTimer.text = "00:00.00"
        amplitudes = binding.waveformView.clear()
    }

    override fun onTimerTick(duration: String) {
        binding.tvTimer.text = duration
        this.duration = duration.dropLast(3)
        binding.waveformView.addAmplitude(recorder.maxAmplitude.toFloat())
    }
}