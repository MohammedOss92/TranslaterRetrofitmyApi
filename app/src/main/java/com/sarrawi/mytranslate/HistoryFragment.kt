package com.sarrawi.mytranslate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sarrawi.mytranslate.adapter.AdapterHistory
import com.sarrawi.mytranslate.api.RetrofitClient
import com.sarrawi.mytranslate.database.AppDatabase
import com.sarrawi.mytranslate.databinding.FragmentHistoryBinding
import com.sarrawi.mytranslate.repo.TranslationRepository
import com.sarrawi.mytranslate.vm.Translate_VM
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdapterHistory
    private lateinit var viewModel: Translate_VM

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


        lifecycleScope.launch {
            val history = historyDao.getAllHistory()
            adapter.submitList(history)
        }

        adapter.onClick= { historyItem,pos ->
            viewModel.deleteHistory(historyItem)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
