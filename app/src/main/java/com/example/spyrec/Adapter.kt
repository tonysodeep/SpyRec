package com.example.spyrec

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date

class Adapter(var records: ArrayList<AudioRecord>, var listener: OnItemClickListener) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {

    private var editMode = false
    fun isEditMode(): Boolean {
        return editMode
    }

    fun setEditMode(mode: Boolean) {
        if (editMode != mode) {
            editMode = mode
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {
        var tvFilename: TextView = itemView.findViewById(R.id.tvFilename)
        var tvMeta: TextView = itemView.findViewById(R.id.tvMeta)
        var checkBox: CheckBox = itemView.findViewById(R.id.checkbox)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION)
                listener.onItemClickListener(position)

        }

        override fun onLongClick(v: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION)
                listener.onItemLongClickListener(position)
            return true
        }
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

            if (editMode) {
                holder.checkBox.visibility = View.VISIBLE
                holder.checkBox.isChecked = record.isChecked
            } else {
                holder.checkBox.visibility = View.GONE
                holder.checkBox.isChecked = false
            }
        }
    }

    override fun getItemCount(): Int {
        return records.size
    }
}