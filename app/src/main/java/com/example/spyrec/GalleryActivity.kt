package com.example.spyrec

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.spyrec.databinding.ActivityGalleryBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity(), OnItemClickListener {
    private lateinit var records: ArrayList<AudioRecord>
    private lateinit var mAdapter: Adapter
    private lateinit var db: AppDatabase
    private lateinit var binding: ActivityGalleryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        records = ArrayList()

        db = Room.databaseBuilder(
            this, AppDatabase::class.java, "audioRecords"
        ).build()

        mAdapter = Adapter(records, this)

        binding.recyclerview.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)
        }

        fetchAll()
    }

    private fun fetchAll() {
        GlobalScope.launch {
            records.clear()
            var queryResult = db.audioRecordDao().getAll()
            records.addAll(queryResult)
            mAdapter.notifyDataSetChanged()
        }
    }

    override fun onItemClickListener(position: Int) {
//        Toast.makeText(this, "Simple click $position", Toast.LENGTH_SHORT).show()
        var audioRecord = records[position]
        var intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra("filepath", audioRecord.filePath)
        intent.putExtra("filename", audioRecord.filename)
        startActivity(intent)
    }

    override fun onItemLongClickListener(position: Int) {
        Toast.makeText(this, "Long click $position", Toast.LENGTH_SHORT).show()
    }
}