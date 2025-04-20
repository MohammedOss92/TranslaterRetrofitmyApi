package com.sarrawi.mytranslate

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sarrawi.mytranslate.api.RetrofitClient
import com.sarrawi.mytranslate.database.AppDatabase
import com.sarrawi.mytranslate.databinding.FragmentTranslateBinding
import com.sarrawi.mytranslate.databinding.FragmentTranslateCameraBinding
import com.sarrawi.mytranslate.model.TranslateRequest
import com.sarrawi.mytranslate.repo.TranslationRepository
import com.sarrawi.mytranslate.util.LanguageCodes
import com.sarrawi.mytranslate.vm.Translate_VM
import kotlinx.coroutines.launch
import java.util.*

class TranslateCameraFragment : Fragment() {

    private var _binding: FragmentTranslateCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: Translate_VM
    private lateinit var tts: TextToSpeech

    private val CAMERA_REQUEST_CODE = 2
    private lateinit var cameraImageUri: Uri

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTranslateCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val db = AppDatabase.getDatabase(requireContext())
        val historyDao = db.historyDao()
        val repository = TranslationRepository(RetrofitClient.apiService, historyDao)
        viewModel = Translate_VM(repository)

        tts = TextToSpeech(requireContext()) {
            if (it == TextToSpeech.SUCCESS) tts.language = Locale("en")
        }

        val languageNames = LanguageCodes.languages.values.toList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languageNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.sourceLanguageSpinner.adapter = adapter
        binding.targetLanguageSpinner.adapter = adapter
        binding.sourceLanguageSpinner.setSelection(languageNames.indexOf("English"))
        binding.targetLanguageSpinner.setSelection(languageNames.indexOf("Arabic"))


        binding.captureImageButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val imageUri = createImageUri()
            if (imageUri != null) {
                cameraImageUri = imageUri
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(requireContext(), "فشل في إنشاء ملف الصورة", Toast.LENGTH_SHORT).show()
            }
        }


        binding.translateButton.setOnClickListener {
            val sourceLangCode = getLanguageCode(binding.sourceLanguageSpinner.selectedItem.toString())
            val targetLangCode = getLanguageCode(binding.targetLanguageSpinner.selectedItem.toString())
            val text = binding.inputText.text.toString()

            if (text.isBlank()) {
                Toast.makeText(requireContext(), "اكتب النص أولًا", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = TranslateRequest(sourceLangCode, targetLangCode, text)
            viewModel.translate_vm2(request) { result ->
                binding.translatedText.setText(result?.translated_text ?: "فشل في الترجمة")
            }
        }

        binding.selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val imageUri: Uri? = data?.data
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                    extractTextFromImage(bitmap)
                }
                CAMERA_REQUEST_CODE -> {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, cameraImageUri)
                    extractTextFromImage(bitmap)
                }
            }
        }
    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
//            val imageUri: Uri? = data.data
//            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
//            extractTextFromImage(bitmap)
//        }
//    }

    private fun extractTextFromImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                binding.inputText.setText(extractedText)

                // نسخ إلى الحافظة
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("extracted text", extractedText)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(requireContext(), "تم استخراج النص ونسخه", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "فشل استخراج النص من الصورة", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getLanguageCode(languageName: String): String {
        return LanguageCodes.languages.entries.firstOrNull { it.value == languageName }?.key ?: "en"
    }

    private fun createImageUri(): Uri? {
        val contentValues = android.content.ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "captured_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

}

//
//    private val CAMERA_REQUEST_CODE = 100
//
//
//    @RequiresApi(Build.VERSION_CODES.P)
//    private val pickImageLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val imageUri = result.data?.data
//                imageUri?.let {
//                    binding.imageView.setImageURI(it)
//                    recognizeTextFromImage(it)
//                }
//            }
//        }
//
//    private val cameraLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val bitmap = result.data?.extras?.get("data") as? Bitmap
//                bitmap?.let {
//                    binding.imageView.setImageBitmap(it)
//                    recognizeTextFromBitmap(it)
//                }
//            }
//        }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentTranslateCameraBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    @RequiresApi(Build.VERSION_CODES.P)
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val languageNames = LanguageCodes.languages.values.toList()
//        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, languageNames)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.sourceLanguageSpinner.adapter = adapter
//        binding.targetLanguageSpinner.adapter = adapter
//
//        // تعيين اللغة الافتراضية
//        binding.sourceLanguageSpinner.setSelection(languageNames.indexOf("English"))
//        binding.targetLanguageSpinner.setSelection(languageNames.indexOf("Arabic"))
//
//        // زر الترجمة
////        binding.btntrand.setOnClickListener {
////            val sourceLangName = binding.sourceLanguageSpinner.selectedItem.toString()
////            val targetLangName = binding.targetLanguageSpinner.selectedItem.toString()
////
////            val sourceLangCode = getLanguageCode(sourceLangName)
////            val targetLangCode = getLanguageCode(targetLangName)
////            //val text = binding.inputText.text.toString()
////
////            if (text.isBlank()) {
////                Toast.makeText(requireContext(), getString(R.string.enter_text), Toast.LENGTH_SHORT).show()
////                return@setOnClickListener
////            }
////
////            val request = TranslateRequest(
////                source_language = sourceLangCode,
////                target_language = targetLangCode,
////                source_text = text
////            )
////
////            viewModel.translate_vm2(request) { result ->
////                if (result != null) {
////                    binding.textViewResult.setText(result.translated_text)
////                } else {
////                    Toast.makeText(requireContext(), getString(R.string.translation_failed), Toast.LENGTH_SHORT).show()
////                }
////            }
////        }
//        binding.textViewResult.setTextIsSelectable(true)
//
//        binding.btnSelectImage.setOnClickListener {
//            showImageDialog()
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.P)
//    private fun showImageDialog() {
//        val options = arrayOf("التقاط صورة بالكاميرا", "اختيار من المعرض")
//        AlertDialog.Builder(requireContext())
//            .setTitle("اختر الطريقة")
//            .setItems(options) { _, which ->
//                when (which) {
//                    0 -> openCamera()
//                    1 -> pickImageFromGallery()
//                }
//            }.show()
//    }
//
//    private fun openCamera() {
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.CAMERA),
//                CAMERA_REQUEST_CODE
//            )
//        } else {
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            cameraLauncher.launch(intent)
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.P)
//    private fun pickImageFromGallery() {
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        pickImageLauncher.launch(intent)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.P)
//    private fun recognizeTextFromImage(imageUri: Uri) {
//        try {
//            val source = ImageDecoder.createSource(requireContext().contentResolver, imageUri)
//            val bitmap = ImageDecoder.decodeBitmap(source)
//            recognizeTextFromBitmap(bitmap)
//        } catch (e: Exception) {
//            Toast.makeText(requireContext(), "حدث خطأ أثناء معالجة الصورة", Toast.LENGTH_SHORT).show()
//            Log.e("TextRecognition", "Error: ", e)
//        }
//    }
//
//    private fun recognizeTextFromBitmap(bitmap: Bitmap) {
//        val inputImage = InputImage.fromBitmap(bitmap, 0)
//        val recognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())
//
//        recognizer.process(inputImage)
//            .addOnSuccessListener { visionText ->
//                binding.textViewResult.text = visionText.text
//            }
//            .addOnFailureListener { e ->
//                binding.textViewResult.text = "فشل في استخراج النص"
//                Log.e("TextRecognition", "Failed: ", e)
//            }
//    }
//
//    private fun getLanguageCode(languageName: String): String {
//        return LanguageCodes.languages.entries.firstOrNull { it.value == languageName }?.key ?: "en"
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
