package com.gitissueapp.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gitissueapp.app.data.model.Label

class LabelsAdapter : RecyclerView.Adapter<LabelsAdapter.LabelViewHolder>() {

    private var labels: List<Label> = emptyList()

    fun updateLabels(newLabels: List<Label>) {
        labels = newLabels
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return LabelViewHolder(view)
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        holder.bind(labels[position])
    }

    override fun getItemCount(): Int = labels.size

    class LabelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(label: Label) {
            textView.text = label.name
            textView.setPadding(16, 8, 16, 8)
            
            // Set background color based on label color
            try {
                val color = Color.parseColor("#${label.color}")
                textView.setBackgroundColor(color)
                
                // Set text color based on background brightness
                val luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
                textView.setTextColor(if (luminance > 0.5) Color.BLACK else Color.WHITE)
            } catch (e: Exception) {
                // Fallback to default colors
                textView.setBackgroundColor(Color.GRAY)
                textView.setTextColor(Color.WHITE)
            }
        }
    }
}