package com.example.spyrec

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.spyrec.databinding.ActivityGalleryBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity(), OnItemClickListener {
    private lateinit var records: ArrayList<AudioRecord>
    private lateinit var mAdapter: Adapter
    private lateinit var db: AppDatabase
    private lateinit var binding: ActivityGalleryBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var allChecked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

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

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var query = s.toString()
                searchDatabase(query)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        binding.btnClose.setOnClickListener {
            leaveEditMode()

        }

        binding.btnSelectAll.setOnClickListener {
            allChecked = !allChecked
            records.map { it.isChecked = allChecked }
            mAdapter.notifyDataSetChanged()
            if (allChecked) {
                disableRename()
                enableDelete()
            } else {
                disableDelete()
                disableRename()
            }
        }

        binding.btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Record ?")
            val nbRecords = records.count { it.isChecked }
            builder.setMessage("Are you sure you want to delete $nbRecords record(s) ?")

            builder.setPositiveButton("Delete") { _, _ ->
                val toDelete = records.filter { it.isChecked }.toTypedArray()
                GlobalScope.launch {
                    db.audioRecordDao().delete(toDelete)
                    runOnUiThread {
                        records.removeAll(toDelete)
                        mAdapter.notifyDataSetChanged()
                        leaveEditMode()
                    }
                }
            }
            builder.setNegativeButton("Cancel") { _, _ -> }
            val dialog = builder.create()
            dialog.show()
        }

    }

    private fun leaveEditMode() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.editBar.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        records.map { it.isChecked = false }
        mAdapter.setEditMode(false)
    }


    private fun disableRename() {
        binding.btnEdit.isClickable = false
        binding.btnEdit.backgroundTintList =
            ResourcesCompat.getColorStateList(resources, R.color.grayDarkDisabled, theme)
        binding.tvEdit.setTextColor(
            ResourcesCompat.getColorStateList(
                resources,
                R.color.grayDarkDisabled,
                theme
            )
        )
    }

    private fun disableDelete() {
        binding.btnDelete.isClickable = false
        binding.tvDelete.backgroundTintList =
            ResourcesCompat.getColorStateList(resources, R.color.grayDarkDisabled, theme)
        binding.tvEdit.setTextColor(
            ResourcesCompat.getColorStateList(
                resources,
                R.color.grayDarkDisabled,
                theme
            )
        )
    }

    private fun enableRename() {
        binding.btnEdit.isClickable = true
        binding.btnEdit.backgroundTintList =
            ResourcesCompat.getColorStateList(resources, R.color.grayDark, theme)
        binding.tvEdit.setTextColor(
            ResourcesCompat.getColorStateList(
                resources,
                R.color.grayDark,
                theme
            )
        )
    }

    private fun enableDelete() {
        binding.btnDelete.isClickable = true
        binding.tvDelete.backgroundTintList =
            ResourcesCompat.getColorStateList(resources, R.color.grayDarkDisabled, theme)
        binding.tvEdit.setTextColor(
            ResourcesCompat.getColorStateList(
                resources,
                R.color.grayDark,
                theme
            )
        )
    }

    private fun searchDatabase(query: String) {
        GlobalScope.launch {
            records.clear()
            var queryResult = db.audioRecordDao().searchDatabase("%$query%")
            records.addAll(queryResult)

            runOnUiThread {
                mAdapter.notifyDataSetChanged()
            }

        }
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
        var audioRecord = records[position]
        if (mAdapter.isEditMode()) {
            records[position].isChecked = !records[position].isChecked
            mAdapter.notifyItemChanged(position)

            var nbSelected = records.count { it.isChecked }
            when (nbSelected) {
                0 -> {
                    disableRename()
                    disableDelete()
                }

                1 -> {
                    enableRename()
                    enableDelete()
                }

                else -> {
                    disableRename()
                    enableDelete()
                }
            }
        } else {
            var intent = Intent(this, AudioPlayerActivity::class.java)
            intent.putExtra("filepath", audioRecord.filePath)
            intent.putExtra("filename", audioRecord.filename)
            startActivity(intent)
        }

    }

    override fun onItemLongClickListener(position: Int) {
//        Toast.makeText(this, "Long click $position", Toast.LENGTH_SHORT).show()
        mAdapter.setEditMode(true)
        records[position].isChecked = !records[position].isChecked
        mAdapter.notifyItemChanged(position)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        if (mAdapter.isEditMode() && binding.editBar.visibility == View.GONE) {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            binding.editBar.visibility = View.VISIBLE
            enableDelete()
            enableRename()
        }
    }
}