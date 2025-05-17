package com.sarrawi.mytranslate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sarrawi.mytranslate.adapter.AdapterHistory
import com.sarrawi.mytranslate.api.RetrofitClient
import com.sarrawi.mytranslate.database.AppDatabase
import com.sarrawi.mytranslate.databinding.FragmentHistoryBinding
import com.sarrawi.mytranslate.db.repository.FavRepo
import com.sarrawi.mytranslate.db.vModel.FavViewModel
import com.sarrawi.mytranslate.db.vModel.ViewModelFactory2
import com.sarrawi.mytranslate.model.FavModel
import com.sarrawi.mytranslate.model.History
import com.sarrawi.mytranslate.repo.TranslationRepository
import com.sarrawi.mytranslate.vm.Translate_VM
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdapterHistory
    private lateinit var viewModel: Translate_VM

    private val a by lazy {  FavRepo(requireActivity().application) }
    private val favviewModel: FavViewModel by viewModels {
        ViewModelFactory2(a)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())

        // الحصول على DAO من قاعدة البيانات
        val historyDao = db.historyDao()

        val repository = TranslationRepository(RetrofitClient.apiService, historyDao)
        viewModel = Translate_VM(repository)

        adapter = AdapterHistory(requireContext())

        binding.historyTrans.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@HistoryFragment.adapter
        }
        viewModel.allHistory.observe(viewLifecycleOwner) { historyList ->
            adapter.submitList(historyList) // تأكد إنك تستخدم AsyncListDiffer أو ListAdapter
        }


//        lifecycleScope.launch {
//            val history = historyDao.getAllHistory()
//            adapter.submitList(history)
//        }

        adapter.onClick= { historyItem,pos ->
            viewModel.deleteHistory(historyItem)
            adapter.notifyDataSetChanged()
        }

        adapter.onItemClick = { historyItem: History, pos: Int ->

            favviewModel.isFavorite(historyItem.word, historyItem.meaning) { isFav ->

                if (!isFav) {
                    favviewModel.addFavorite(
                        FavModel(
                            id = historyItem.id,
                            word = historyItem.word,
                            meaning = historyItem.meaning,
                            sourceLang = historyItem.sourceLang,
                            targetLang = historyItem.targetLang
                        )
                    )
                    historyItem.is_fav = true
                    viewModel.updateIsFav(historyItem.word, historyItem.meaning, true)

                    Snackbar.make(requireView(), "تمت الإضافة إلى المفضلة", Snackbar.LENGTH_SHORT).show()
                    historyItem.is_fav = true
                } else {
                    favviewModel.removeFavorite(historyItem.word, historyItem.meaning)
                    historyItem.is_fav = false
                    viewModel.updateIsFav(historyItem.word, historyItem.meaning, false)

                    Snackbar.make(requireView(), "تمت الإزالة من المفضلة", Snackbar.LENGTH_SHORT).show()
                    historyItem.is_fav = false
                }

                // تحديث القائمة بعد تعديل العنصر
                val updatedList = adapter.currentList.toMutableList()
                updatedList[pos] = historyItem
                adapter.submitList(updatedList)
            }
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
