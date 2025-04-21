package com.sarrawi.mytranslate

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sarrawi.mytranslate.databinding.FragmentTranslateCameraBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TranslateCameraFragment : Fragment() {

    private var _binding: FragmentTranslateCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var editText: EditText
    private lateinit var cameraExecutor: ExecutorService

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { processImageFromUri(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTranslateCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewView = view.findViewById(R.id.previewView)
        editText = view.findViewById(R.id.editText)
        val captureBtn: Button = view.findViewById(R.id.captureBtn)
        val selectImageBtn: Button = view.findViewById(R.id.selectImageBtn)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                101
            )
        }

        captureBtn.setOnClickListener {
            capturePhoto()
        }

        selectImageBtn.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun capturePhoto() {
        val photoFile = File(requireContext().externalCacheDir, "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    recognizeText(bitmap)
                }

                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), "فشل في التقاط الصورة", Toast.LENGTH_SHORT).show()
                }
            }
        )
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
                editText.setText(visionText.text)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "فشل في قراءة النص", Toast.LENGTH_SHORT).show()
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}

//package com.sarrawi.mytranslate
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.net.Uri
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.text.TextRecognition
//import com.google.mlkit.vision.text.latin.TextRecognizerOptions
//import com.sarrawi.mytranslate.databinding.FragmentTranslateCameraBinding
//import java.io.File
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//class TranslateCameraFragment : Fragment() {
//
//
//    private var _binding: FragmentTranslateCameraBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var previewView: PreviewView
//    private lateinit var imageCapture: ImageCapture
//    private lateinit var editText: EditText
//    private lateinit var cameraExecutor: ExecutorService
//
//
//    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        uri?.let { processImageFromUri(it) }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentTranslateCameraBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        previewView = view.findViewById(R.id.previewView)
//        editText = view.findViewById(R.id.editText)
//        val captureBtn: Button = view.findViewById(R.id.captureBtn)
//        val selectImageBtn: Button = view.findViewById(R.id.selectImageBtn)
//
//        cameraExecutor = Executors.newSingleThreadExecutor()
//
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
//                101
//            )
//        }
//
//        captureBtn.setOnClickListener {
//            capturePhoto()
//        }
//
//        selectImageBtn.setOnClickListener {
//            pickImage.launch("image/*")
//        }
//    }
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//
//        cameraProviderFuture.addListener({
//            val cameraProvider = cameraProviderFuture.get()
//
//            val preview = Preview.Builder().build().also {
//                it.setSurfaceProvider(previewView.surfaceProvider)
//            }
//
//            imageCapture = ImageCapture.Builder().build()
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            cameraProvider.unbindAll()
//            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
//
//        }, ContextCompat.getMainExecutor(requireContext()))
//    }
//
//    private fun capturePhoto() {
//        val photoFile = File(requireContext().externalCacheDir, "${System.currentTimeMillis()}.jpg")
//        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
//
//        imageCapture.takePicture(
//            outputOptions,
//            ContextCompat.getMainExecutor(requireContext()),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
//                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
//                    recognizeText(bitmap)
//                }
//
//                override fun onError(exc: ImageCaptureException) {
//                    Toast.makeText(requireContext(), "فشل في التقاط الصورة", Toast.LENGTH_SHORT).show()
//                }
//            }
//        )
//    }
//
//    private fun processImageFromUri(uri: Uri) {
//        val inputStream = requireContext().contentResolver.openInputStream(uri)
//        val bitmap = BitmapFactory.decodeStream(inputStream)
//        recognizeText(bitmap)
//    }
//
//    private fun recognizeText(bitmap: Bitmap) {
////        val image = InputImage.fromBitmap(bitmap, 0)
////
////        val recognizer = TextRecognition.getClient(
////            ArabicTextRecognizerOptions.Builder().build()
////        )
////
////        recognizer.process(image)
////            .addOnSuccessListener { visionText ->
////                editText.setText(visionText.text)
////            }
////            .addOnFailureListener {
////                Toast.makeText(requireContext(), "فشل في قراءة النص", Toast.LENGTH_SHORT).show()
////            }
//        val image = InputImage.fromBitmap(bitmap, 0)
//        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
//
//        recognizer.process(image)
//            .addOnSuccessListener { visionText ->
//                editText.setText(visionText.text)
//            }
//            .addOnFailureListener {
//                Toast.makeText(requireContext(), "فشل في قراءة النص", Toast.LENGTH_SHORT).show()
//            }
//
//    }
//
//    private fun allPermissionsGranted(): Boolean {
//        val permissions = arrayOf(
//            Manifest.permission.CAMERA,
//            Manifest.permission.READ_EXTERNAL_STORAGE
//        )
//        return permissions.all {
//            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//        cameraExecutor.shutdown()
//    }
//}
