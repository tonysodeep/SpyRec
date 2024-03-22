package com.example.spyrec

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date

class Adapter(var records: ArrayList<AudioRecord>) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvFilename: TextView = itemView.findViewById(R.id.tvFilename)
        var tvMeta: TextView = itemView.findViewById(R.id.tvMeta)
        var checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.itemview_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            var record = records[position]
            holder.tvFilename.text = record.filename

            var sdf = SimpleDateFormat("dd/MM/yyyy")
            var date = Date(record.timestamp)
            var strDate = sdf.format(date)
            holder.tvMeta.text = "${record.duration} $strDate"
        }
    }

    override fun getItemCount(): Int {
        return records.size
    }
}