package com.sarrawi.mytranslate.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sarrawi.mytranslate.databinding.FavLayBinding
import com.sarrawi.mytranslate.model.FavModel

class FavAdapter(private val context: Context) : RecyclerView.Adapter<FavAdapter.MyViewHolder>() {

    var onItemClick: ((fav: FavModel) -> Unit)? = null

    inner class MyViewHolder(private val binding: FavLayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FavModel) {
            binding.sourceText.text = item.word
            binding.translatedText.text = item.meaning
            binding.sourceLang.text = item.sourceLang
            binding.targetLang.text = item.targetLang

            binding.favbtn.setOnClickListener {
                onItemClick?.invoke(item) // الأفضل تمرير العنصر مباشرة
            }


        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<FavModel>() {
        override fun areItemsTheSame(oldItem: FavModel, newItem: FavModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FavModel, newItem: FavModel): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var trans_fav_list: List<FavModel>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = FavLayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = trans_fav_list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = trans_fav_list.size
}
