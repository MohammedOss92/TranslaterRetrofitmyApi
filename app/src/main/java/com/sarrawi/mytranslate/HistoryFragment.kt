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

            favviewModel.getFav().observe(viewLifecycleOwner) { favs ->

                //متى تستخدم any:
                //تستخدم any عندما تريد أن تتحقق:
                //
                //هل يوجد عنصر واحد على الأقل داخل قائمة يحقق شرط معين؟

                // متى لا تستخدم any:
                //إذا كنت تريد كل العناصر تحقق شرط → استخدم all.
                //
                //إذا كنت تريد عنصر واحد محدد → استخدم find أو firstOrNull.
                //
                //إذا كنت تريد فقط عدد العناصر → استخدم count.
                //الدالة	وظيفتها
                //any	يرجع true إذا أي عنصر يحقق الشرط.
                //all	يرجع true إذا كل العناصر تحقق الشرط.
                //none	يرجع true إذا ولا عنصر يحقق الشرط.
                //find	يرجع أول عنصر يحقق الشرط أو null إذا ما وجد.
                val updatedList = historyList.map { historyItem ->
                    val isFav = favs.any { it.word == historyItem.word && it.meaning == historyItem.meaning }
                    historyItem.copy(is_fav = isFav)
                }

                adapter.favoriteList = favs
                adapter.submitList(updatedList)
            }
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
