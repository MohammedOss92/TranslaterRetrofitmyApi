package com.sarrawi.mytranslate

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sarrawi.mytranslate.databinding.FragmentPreviewBinding

class PreviewFragment : Fragment() {
    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // استلام URI من SafeArgs
        val args = PreviewFragmentArgs.fromBundle(requireArguments())
        val uriString = args.imageUri
        val imageUri = Uri.parse(uriString)

        val bitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(imageUri))
        binding.imageView.setImageBitmap(bitmap)

        binding.extractTextButton.setOnClickListener {
            recognizeText(bitmap)
        }
    }

    private fun recognizeText(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                binding.resultEditText.setText(visionText.text)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "فشل في قراءة النص", Toast.LENGTH_SHORT).show()
            }
    }
}
