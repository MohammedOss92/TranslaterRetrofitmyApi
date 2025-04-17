package com.sarrawi.mytranslate

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.sarrawi.mytranslate.api.RetrofitClient
import com.sarrawi.mytranslate.databinding.FragmentTranslateBinding
import com.sarrawi.mytranslate.model.TranslateRequest
import com.sarrawi.mytranslate.repo.TranslationRepository
import com.sarrawi.mytranslate.util.LanguageCodes
import com.sarrawi.mytranslate.vm.Translate_VM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TranslateFragment : Fragment() {

    private var _binding: FragmentTranslateBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: Translate_VM

    private val languages = listOf("العربية", "الإنجليزية", "الفرنسية", "الإسبانية")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTranslateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = TranslationRepository(RetrofitClient.apiService)
        viewModel = Translate_VM(repository)

        // إعداد الـ Spinners
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sourceLanguageSpinner.adapter = adapter
        binding.targetLanguageSpinner.adapter = adapter

        binding.sourceLanguageSpinner.setSelection(1) // الإنجليزية
        binding.targetLanguageSpinner.setSelection(0) // العربية

        // تبديل اللغات
        binding.switchLanguages.setOnCheckedChangeListener { _, _ ->
            val sourcePos = binding.sourceLanguageSpinner.selectedItemPosition
            val targetPos = binding.targetLanguageSpinner.selectedItemPosition
            binding.sourceLanguageSpinner.setSelection(targetPos)
            binding.targetLanguageSpinner.setSelection(sourcePos)
        }

        // زر الترجمة
        binding.translateButton.setOnClickListener {
            val sourceLangName = binding.sourceLanguageSpinner.selectedItem.toString()
            val targetLangName = binding.targetLanguageSpinner.selectedItem.toString()

            val sourceLangCode = LanguageCodes.languages[sourceLangName] ?: "en"
            val targetLangCode = LanguageCodes.languages[targetLangName] ?: "ar"
            val text = binding.inputText.text.toString()

            if (text.isBlank()) {
                Toast.makeText(requireContext(), "اكتب النص أولًا", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = TranslateRequest(
                source_language = sourceLangCode,
                target_language = targetLangCode,
                source_text = text
            )

            viewModel.translate_vm2(request) { result ->
                if (result != null) {
                    binding.translatedText.setText(result.translated_text)
                } else {
                    Toast.makeText(requireContext(), "فشل في الترجمة", Toast.LENGTH_SHORT).show()
                }



//            lifecycleScope.launch {
//                val request = TranslateRequest(
//                    source_language = sourceLangCode,
//                    target_language = targetLangCode,
//                    source_text = text
//                )
//
//                viewModel.translate_vm(request)  // سيتم استخدام callback عند انتهاء الترجمة
            }}


            // زر النسخ
            binding.copyButton.setOnClickListener {
                val textToCopy = binding.translatedText.text.toString()
                if (textToCopy.isNotEmpty()) {
                    val clipboard =
                        requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("translated text", textToCopy)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(requireContext(), "تم نسخ الترجمة", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "لا يوجد نص لنسخه", Toast.LENGTH_SHORT).show()
                }
            }
        }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


//package com.sarrawi.mytranslate
//
//import android.content.ClipData
//import android.content.ClipboardManager
//import android.content.Context
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import androidx.fragment.app.Fragment
//import com.sarrawi.mytranslate.databinding.FragmentTranslateBinding
//import java.util.*
//
//class TranslateFragment : Fragment() {
//
//    private var _binding: FragmentTranslateBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var sourceSpinner: Spinner
//    private lateinit var targetSpinner: Spinner
//    private lateinit var switchLanguages: Switch
//    private lateinit var inputText: EditText
//    private lateinit var translatedText: EditText
//    private lateinit var copyButton: Button
//
//    private val languages: ArrayList<String> = ArrayList<String>().apply {
//        add("العربية")
//        add("الإنجليزية")
//        add("الفرنسية")
//        add("الإسبانية")
//    }
//    private val languagess = arrayListOf("العربية", "الإنجليزية", "الفرنسية", "الإسبانية")
//
//    private val languagesa = listOf("العربية", "الإنجليزية", "الفرنسية", "الإسبانية")
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentTranslateBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // ربط عناصر الواجهة
//        sourceSpinner = view.findViewById(R.id.sourceLanguageSpinner)
//        targetSpinner = view.findViewById(R.id.targetLanguageSpinner)
//        switchLanguages = view.findViewById(R.id.switchLanguages)
//        inputText = view.findViewById(R.id.inputText)
//        translatedText = view.findViewById(R.id.translatedText)
//        copyButton = view.findViewById(R.id.copyButton)
//
//        // إعداد الـ Spinners باللغات
//        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        sourceSpinner.adapter = adapter
//        targetSpinner.adapter = adapter
//
//        // تعيين القيم الافتراضية
//        sourceSpinner.setSelection(1) // الإنجليزية
//        targetSpinner.setSelection(0) // العربية
//
//        // تبديل اللغات عند الضغط على السويتش
//        switchLanguages.setOnCheckedChangeListener { _, _ ->
//            val sourcePosition = sourceSpinner.selectedItemPosition
//            val targetPosition = targetSpinner.selectedItemPosition
//            sourceSpinner.setSelection(targetPosition)
//            targetSpinner.setSelection(sourcePosition)
//        }
//
//        // زر نسخ الترجمة
//        val textToCopy = translatedText.text.toString()
//        if (textToCopy.isNotEmpty()) {
//            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//            val clip = ClipData.newPlainText("translated text", textToCopy)
//            clipboard.setPrimaryClip(clip)
//            Toast.makeText(requireContext(), "تم نسخ الترجمة", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(requireContext(), "لا يوجد نص لنسخه", Toast.LENGTH_SHORT).show()
//        }
//
//    }
//
//
//        override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
