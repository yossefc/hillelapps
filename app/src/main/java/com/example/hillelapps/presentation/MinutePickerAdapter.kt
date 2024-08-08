package com.example.hillelapps.presentation

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MinutePickerAdapter(
    private val minutes: List<Int>,
    private val onMinuteSelected: (Int) -> Unit
) : RecyclerView.Adapter<MinutePickerAdapter.ViewHolder>() {

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            textSize = 18f
            setTextColor(Color.WHITE)
        }
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val minute = minutes[position]
        holder.textView.text = "$minute min"
        holder.itemView.setOnClickListener { onMinuteSelected(minute) }
    }

    override fun getItemCount() = minutes.size
}