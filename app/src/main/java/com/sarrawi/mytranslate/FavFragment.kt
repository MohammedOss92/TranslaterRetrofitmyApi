package com.sarrawi.mytranslate

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sarrawi.mytranslate.adapter.AdapterHistory
import com.sarrawi.mytranslate.adapter.FavAdapter
import com.sarrawi.mytranslate.databinding.FragmentFavBinding
import com.sarrawi.mytranslate.db.repository.FavRepo
import com.sarrawi.mytranslate.db.vModel.FavViewModel
import com.sarrawi.mytranslate.db.vModel.ViewModelFactory2
import kotlinx.coroutines.launch


class FavFragment : Fragment() {

    private var _binding:FragmentFavBinding ?=null
    private val binding get() = _binding!!

    private val favAdapter by lazy { FavAdapter(requireContext()) }
    private val a by lazy {  FavRepo(requireActivity().application) }
    private val favviewModel: FavViewModel by viewModels {
        ViewModelFactory2(a)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFavBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRv()
        binding.etsearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                favviewModel.search(query).observe(viewLifecycleOwner) {
                    favAdapter.trans_fav_list=it
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }

        private fun setUpRv() = favviewModel.viewModelScope.launch {

            binding.FavRec.layoutManager = LinearLayoutManager(requireContext())
            binding.FavRec.adapter = favAdapter

            favAdapter.onItemClick = {
                favviewModel.update_fav(it.id, false)
                favviewModel.removeFavorite(it.word, it.meaning)
            }

            favviewModel.getFav().observe(viewLifecycleOwner) { listTvShows ->
                favAdapter.trans_fav_list = listTvShows
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}