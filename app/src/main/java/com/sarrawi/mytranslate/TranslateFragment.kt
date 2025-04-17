package com.sarrawi.mytranslate

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import java.util.*

class TranslateFragment : Fragment() {

    private var _binding: FragmentTranslateBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: Translate_VM

    private lateinit var tts: TextToSpeech

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

        // تهيئة النطق
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // تعيين اللغة الافتراضية للنطق (الإنجليزية)
                tts.language = Locale("en")
            }
        }

        // إعداد الـ Spinners مع جميع اللغات
        val languageNames = LanguageCodes.languages.values.toList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languageNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sourceLanguageSpinner.adapter = adapter
        binding.targetLanguageSpinner.adapter = adapter

        // تعيين اللغة الافتراضية
        binding.sourceLanguageSpinner.setSelection(languageNames.indexOf("English"))
        binding.targetLanguageSpinner.setSelection(languageNames.indexOf("Arabic"))

        // زر الترجمة
        binding.translateButton.setOnClickListener {
            val sourceLangName = binding.sourceLanguageSpinner.selectedItem.toString()
            val targetLangName = binding.targetLanguageSpinner.selectedItem.toString()

            val sourceLangCode = getLanguageCode(sourceLangName)
            val targetLangCode = getLanguageCode(targetLangName)
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
            }
        }

        // زر النسخ
        binding.copyButton.setOnClickListener {
            val textToCopy = binding.translatedText.text.toString()
            if (textToCopy.isNotEmpty()) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("translated text", textToCopy)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "تم نسخ الترجمة", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "لا يوجد نص لنسخه", Toast.LENGTH_SHORT).show()
            }
        }

        binding.shareButton.setOnClickListener {
            val textToShare = binding.translatedText.text.toString()
            if (textToShare.isNotEmpty()) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, textToShare)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "مشاركة الترجمة عبر"))
            } else {
                Toast.makeText(requireContext(), "لا يوجد نص لمشاركته", Toast.LENGTH_SHORT).show()
            }
        }


        // إعداد النطق بناءً على اللغة المختارة
        binding.speakButton.setOnClickListener {
            val text = binding.translatedText.text.toString()
            if (text.isNotEmpty()) {
                val targetLangName = binding.targetLanguageSpinner.selectedItem.toString()
                val langCode = getLanguageCode(targetLangName)
                val locale = Locale(langCode)

                if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                    tts.language = locale
                } else {
                    tts.language = Locale("en") // إذا كانت اللغة غير مدعومة، استخدم الإنجليزية
                }
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }

        val swapAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate)

        binding.swapLanguagesButton.setOnClickListener {
            it.startAnimation(swapAnimation)

            val sourcePosition = binding.sourceLanguageSpinner.selectedItemPosition
            val targetPosition = binding.targetLanguageSpinner.selectedItemPosition

            binding.sourceLanguageSpinner.setSelection(targetPosition)
            binding.targetLanguageSpinner.setSelection(sourcePosition)
        }

        binding.pasteButton.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val pasteText = clipData.getItemAt(0).text.toString()
                binding.inputText.setText(pasteText)
                Toast.makeText(requireContext(), "تم لصق النص", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "لا يوجد نص في الحافظة", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // دالة للحصول على رمز اللغة بناءً على اسم اللغة
    private fun getLanguageCode(languageName: String): String {
        return LanguageCodes.languages.entries.firstOrNull { it.value == languageName }?.key ?: "en"
    }
}



//package com.sarrawi.mytranslate
//
//import android.content.ClipData
//import android.content.ClipboardManager
//import android.content.Context
//import android.os.Bundle
//import android.speech.tts.TextToSpeech
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import com.sarrawi.mytranslate.api.RetrofitClient
//import com.sarrawi.mytranslate.databinding.FragmentTranslateBinding
//import com.sarrawi.mytranslate.model.TranslateRequest
//import com.sarrawi.mytranslate.repo.TranslationRepository
//import com.sarrawi.mytranslate.util.LanguageCodes
//import com.sarrawi.mytranslate.vm.Translate_VM
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.util.*
//
//class TranslateFragment : Fragment() {
//
//    private var _binding: FragmentTranslateBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var viewModel: Translate_VM
//
//    private val languages = listOf("العربية", "الإنجليزية", "الفرنسية", "الإسبانية")
//
//    private lateinit var tts: TextToSpeech
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
//        val repository = TranslationRepository(RetrofitClient.apiService)
//        viewModel = Translate_VM(repository)
//
//        // تهيئة النطق
//        tts = TextToSpeech(requireContext()) { status ->
//            if (status == TextToSpeech.SUCCESS) {
//                // تعيين اللغة الافتراضية للنطق (على سبيل المثال العربية)
//                tts.language = Locale("ar")
//            }
//        }
//
//        binding.speakButton.setOnClickListener {
//            val text = binding.translatedText.text.toString()
//            if (text.isNotEmpty()) {
//                val selectedLangCode = getLanguageCode(binding.targetLanguageSpinner.selectedItem.toString())
//                val locale = Locale(selectedLangCode)
//                val langAvailable = tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE
//                if (langAvailable) {
//                    tts.language = locale
//                } else {
//                    tts.language = Locale("en") // إذا كانت اللغة غير مدعومة، استخدم الإنجليزية
//                }
//                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
//            }
//        }
//
//        // إعداد الـ Spinners
//        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.sourceLanguageSpinner.adapter = adapter
//        binding.targetLanguageSpinner.adapter = adapter
//
//        binding.sourceLanguageSpinner.setSelection(1) // الإنجليزية
//        binding.targetLanguageSpinner.setSelection(0) // العربية
//
//        // تبديل اللغات
//        binding.switchLanguages.setOnCheckedChangeListener { _, _ ->
//            val sourcePos = binding.sourceLanguageSpinner.selectedItemPosition
//            val targetPos = binding.targetLanguageSpinner.selectedItemPosition
//            binding.sourceLanguageSpinner.setSelection(targetPos)
//            binding.targetLanguageSpinner.setSelection(sourcePos)
//        }
//
//        // زر الترجمة
//        binding.translateButton.setOnClickListener {
//            val sourceLangName = binding.sourceLanguageSpinner.selectedItem.toString()
//            val targetLangName = binding.targetLanguageSpinner.selectedItem.toString()
//
//            val sourceLangCode = LanguageCodes.languages[sourceLangName] ?: "en"
//            val targetLangCode = LanguageCodes.languages[targetLangName] ?: "ar"
//            val text = binding.inputText.text.toString()
//
//            if (text.isBlank()) {
//                Toast.makeText(requireContext(), "اكتب النص أولًا", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val request = TranslateRequest(
//                source_language = sourceLangCode,
//                target_language = targetLangCode,
//                source_text = text
//            )
//
//            viewModel.translate_vm2(request) { result ->
//                if (result != null) {
//                    binding.translatedText.setText(result.translated_text)
//                } else {
//                    Toast.makeText(requireContext(), "فشل في الترجمة", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        // زر النسخ
//        binding.copyButton.setOnClickListener {
//            val textToCopy = binding.translatedText.text.toString()
//            if (textToCopy.isNotEmpty()) {
//                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                val clip = ClipData.newPlainText("translated text", textToCopy)
//                clipboard.setPrimaryClip(clip)
//                Toast.makeText(requireContext(), "تم نسخ الترجمة", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(requireContext(), "لا يوجد نص لنسخه", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    // دالة للحصول على رمز اللغة بناءً على اسم اللغة
//    private fun getLanguageCode(languageName: String): String {
//        return when (languageName) {
//            "العربية" -> "ar"
//            "الإنجليزية" -> "en"
//            "الفرنسية" -> "fr"
//            "الإسبانية" -> "es"
//            else -> "en" // إذا كانت اللغة غير معروفة نضعها الإنجليزية
//        }
//    }
//}
//
