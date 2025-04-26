package com.sarrawi.mytranslate

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sarrawi.mytranslate.api.RetrofitClient
import com.sarrawi.mytranslate.database.AppDatabase
import com.sarrawi.mytranslate.databinding.FragmentTranslateBinding
import com.sarrawi.mytranslate.model.TranslateRequest
import com.sarrawi.mytranslate.repo.TranslationRepository
import com.sarrawi.mytranslate.util.LanguageCodes
import com.sarrawi.mytranslate.vm.Translate_VM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
//
class TranslateFragment : Fragment() {

    private var _binding: FragmentTranslateBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: Translate_VM

    private lateinit var tts: TextToSpeech
    private var isSpeaking = false

    //cam
    private lateinit var imageUri: Uri
    companion object {
        const val CAMERA_REQUEST_CODE = 1001
        const val GALLERY_REQUEST_CODE = 1002
    }
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Passing URI as an argument to the PreviewFragment
            val action = TranslateFragmentDirections.actionTranslateFragmentToPreviewFragment(it.toString())
            findNavController().navigate(action)
        }
    }
    //

    private lateinit var textToTranslate: String

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { processImageFromUri(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTranslateBinding.inflate(inflater, container, false)
        return binding.root
    }
//
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    textToTranslate = arguments?.getString("textToTranslate") ?: ""

    // ضع النص داخل EditText
    binding.inputText.setText(textToTranslate)

        // الحصول على قاعدة البيانات
        val db = AppDatabase.getDatabase(requireContext())

        // الحصول على DAO من قاعدة البيانات
        val historyDao = db.historyDao()

        val repository = TranslationRepository(RetrofitClient.apiService, historyDao)
        viewModel = Translate_VM(repository)

        // تهيئة النطق
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // تعيين اللغة الافتراضية للنطق (الإنجليزية)
                tts.language = Locale("en")
            }
        }

//jhjhj


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
                    binding.translatedText.visibility = View.VISIBLE
                    binding.speakButton.visibility = View.VISIBLE
                    binding.shareButton.visibility = View.VISIBLE
                    binding.copyButton.visibility = View.VISIBLE
                } else {
                    Toast.makeText(requireContext(), "فشل في الترجمة", Toast.LENGTH_SHORT).show()
                }
            }

        }

        binding.inputText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                binding.translateButton.isEnabled = true
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

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




// الاستماع لانتهاء النطق
    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            isSpeaking = true
            requireActivity().runOnUiThread {
                binding.speakButton.setImageResource(R.drawable.ic_paus)
            }
        }

        override fun onDone(utteranceId: String?) {
            isSpeaking = false
            requireActivity().runOnUiThread {
                binding.speakButton.setImageResource(R.drawable.ic_volume)
            }
        }

        override fun onError(utteranceId: String?) {
            isSpeaking = false
            requireActivity().runOnUiThread {
                binding.speakButton.setImageResource(R.drawable.ic_volume)
            }
        }
    })


    binding.speakButton.setOnClickListener {
        val text = binding.translatedText.text.toString()

        if (isSpeaking) {
            tts.stop()
            binding.speakButton.setImageResource(R.drawable.ic_volume)
            isSpeaking = false
            return@setOnClickListener
        }

        if (text.isNotEmpty()) {
            val targetLangName = binding.targetLanguageSpinner.selectedItem.toString()
            val langCode = getLanguageCode(targetLangName)
            val locale = Locale(langCode)

            tts.language = if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                locale
            } else {
                Locale("en")
            }

            val params = Bundle()
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "TTS_ID")
        } else {
            binding.speakButton.isEnabled = false
        }
    }


    // إعداد النطق بناءً على اللغة المختارة
//        binding.speakButton.setOnClickListener {
//            val text = binding.translatedText.text.toString()
//            if (text.isNotEmpty()) {
//                val targetLangName = binding.targetLanguageSpinner.selectedItem.toString()
//                val langCode = getLanguageCode(targetLangName)
//                val locale = Locale(langCode)
//
//                if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
//                    tts.language = locale
//                } else {
//                    tts.language = Locale("en") // إذا كانت اللغة غير مدعومة، استخدم الإنجليزية
//                }
//                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
//            }else{
//                binding.speakButton.isEnabled=false
//            }
//        }

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

//    binding.camButton.setOnClickListener {
//
//        if (allPermissionsGranted()) {
//            pickImage.launch("image/*")
//        } else {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
//                101
//            )
//        }
//    }

    //cam
    binding.camButton.setOnClickListener {
        showImageSourceDialog()
    }


    binding.clearButton.setOnClickListener {
        binding.inputText.text.clear()
        binding.translatedText.text.clear()

        binding.translatedText.visibility = View.GONE
        binding.speakButton.visibility = View.GONE
        binding.shareButton.visibility = View.GONE
        binding.copyButton.visibility = View.GONE
        binding.clearButton.visibility = View.GONE
    }


}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun processImageFromUri(uri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        recognizeText(bitmap)
    }

    private fun recognizeText(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                binding.inputText.setText(visionText.text)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "فشل في قراءة النص", Toast.LENGTH_SHORT).show()
            }
    }

    // دالة للحصول على رمز اللغة بناءً على اسم اللغة
    private fun getLanguageCode(languageName: String): String {
        return LanguageCodes.languages.entries.firstOrNull { it.value == languageName }?.key ?: "en"
    }

    private fun allPermissionsGranted(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /////cam
//    private fun showImageSourceDialog() {
//        val options = arrayOf("التقاط صورة", "تحميل من المعرض")
//        AlertDialog.Builder(requireContext())
//            .setTitle("اختر مصدر الصورة")
//            .setItems(options) { _, which ->
//                when (which) {
//                    0 -> openCamera()
//                    1 -> pickImageLauncher.launch("image/*")
//                }
//            }
//            .show()
//    }
//    private fun openCamera() {
//        val imageFile = File.createTempFile("IMG_", ".jpg", requireContext().cacheDir)
//        imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", imageFile)
//
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
//        startActivityForResult(intent, CAMERA_REQUEST_CODE)
//    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    // Passing URI to PreviewFragment
                    val action = TranslateFragmentDirections.actionTranslateFragmentToPreviewFragment(imageUri.toString())
                    findNavController().navigate(action)
                }
            }
        }
    }




    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "يجب السماح باستخدام الكاميرا", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("التقاط صورة", "تحميل من المعرض")
        AlertDialog.Builder(requireContext())
            .setTitle("اختر مصدر الصورة")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpenCamera()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            // نطلب الصلاحية
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val imageFile = File.createTempFile("IMG_", ".jpg", requireContext().cacheDir)
        imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", imageFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

}


