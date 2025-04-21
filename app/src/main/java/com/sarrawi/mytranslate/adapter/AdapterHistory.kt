package com.sarrawi.mytranslate.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sarrawi.mytranslate.databinding.HistoryDesignBinding
import com.sarrawi.mytranslate.model.History

class AdapterHistory(val con: Context) : ListAdapter<History, AdapterHistory.HistoryViewHolder>(DiffCallback()) {

    var onClick: ((History, Int) -> Unit)? = null

    inner class HistoryViewHolder(private val binding: HistoryDesignBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: History) {
            binding.sourceText.text = item.word
            binding.translatedText.text = item.meaning
            binding.sourceLang.text = item.sourceLang
            binding.targetLang.text = item.targetLang

            binding.deleteButton.setOnClickListener {

                onClick?.invoke(item, adapterPosition)
                notifyDataSetChanged()
                notifyItemChanged(adapterPosition)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<History>() {
        override fun areItemsTheSame(oldItem: History, newItem: History) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: History, newItem: History) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = HistoryDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
