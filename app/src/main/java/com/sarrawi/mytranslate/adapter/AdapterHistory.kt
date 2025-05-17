package com.sarrawi.mytranslate.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sarrawi.mytranslate.R
import com.sarrawi.mytranslate.databinding.HistoryDesignBinding
import com.sarrawi.mytranslate.model.History

class AdapterHistory(val con: Context) : ListAdapter<History, AdapterHistory.HistoryViewHolder>(DiffCallback()) {

    var onClick: ((History, Int) -> Unit)? = null
    var onItemClick: ((item:History,position:Int) -> Unit)? = null

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



            binding.favbtn.setOnClickListener {

                onItemClick?.invoke(item, adapterPosition)
                notifyItemChanged(adapterPosition)
            }

            if (item.is_fav) {
                binding.favbtn.setImageResource(R.drawable.is_fav)

            } else {
                binding.favbtn.setImageResource(R.drawable.not_fav)
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
