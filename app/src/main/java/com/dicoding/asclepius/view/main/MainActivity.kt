package com.dicoding.asclepius.view.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.asclepius.R
import com.dicoding.asclepius.ViewModelFactory
import com.dicoding.asclepius.data.local.Asclepius
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.DateHelper
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.view.detailnews.DetailNewsActivity
import com.dicoding.asclepius.view.history.HistoryActivity
import com.dicoding.asclepius.view.result.ResultActivity
import com.dicoding.asclepius.view.result.ResultActivity.Companion.EXTRA_CONFIDENCE_SCORE
import com.dicoding.asclepius.view.result.ResultActivity.Companion.EXTRA_IMAGE_URI
import com.dicoding.asclepius.view.result.ResultActivity.Companion.EXTRA_RESULT_TEXT
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private var currentImageUri: Uri? = null
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private var resultText: String? = null
    private var confidenceScore: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mainViewModel = obtainViewModel(this)

        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        showToast("Error: $error")
                    }
                }

                override fun onResult(result: List<Classifications>?, inferenceTime: Long) {
                    runOnUiThread {
                        showProgressIndicator(false)
                        displayResults(result, inferenceTime)
                        moveToResult()
                    }
                }
            }
        )

        isLoading()
        setListeners()
        optionMenu()
        showImage()
        getNews()
    }

    private fun setListeners() {
        binding.apply {
            galleryButton.setOnClickListener {
                startGallery()
            }
            analyzeButton.setOnClickListener {
                analyzeImage()
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            startCrop(uri)
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }


    private fun showImage() {
        mainViewModel.currentImageUri.observe(this) { uri ->
            if (uri != null) {
                binding.previewImageView.setImageURI(null)
                binding.previewImageView.setImageURI(uri)
                currentImageUri = uri
            } else {
                Log.d("Photo Picker", "No media selected")
            }
        }
    }

    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = result.data?.let { UCrop.getOutput(it) }
            if (resultUri != null) {
                mainViewModel.currentImageUri.value = resultUri
                showImage()
            } else {
                showToast(getString(R.string.crop_failed))
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = result.data?.let { UCrop.getError(it) }
            cropError?.let { showToast("Crop error: ${it.message}") }
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_img_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            setCompressionQuality(80)
            setToolbarTitle(getString(R.string.crop_image))
        }

        val uCropIntent = UCrop.of(uri, destinationUri)
            .withOptions(options)
            .getIntent(this)

        cropImageLauncher.launch(uCropIntent)
    }

    private fun analyzeImage() {
        currentImageUri?.let { uri ->
            try {
                showProgressIndicator(true)
                imageClassifierHelper.classifyImage(uri)
            } catch (e: Exception) {
                showToast("Failed to analyze image: ${e.message}")
                showProgressIndicator(false)
            }
        } ?: run {
            showToast(getString(R.string.select_an_image))
        }
    }

    private fun displayResults(result: List<Classifications>?, inferenceTime: Long) {
        if (!result.isNullOrEmpty() && result[0].categories.isNotEmpty()) {
            val sortedCategories = result[0].categories.sortedByDescending { it?.score }
            val topCategory = sortedCategories.firstOrNull()

            if (topCategory != null) {
                val isCancer = topCategory.label.equals("Cancer", ignoreCase = true)
                confidenceScore = topCategory.score * 100

                resultText = if (isCancer) {
                    getString(R.string.cancer)
                } else {
                    getString(R.string.non_cancer)
                }
            } else {
                showToast(getString(R.string.no_relevant_results_found))
            }
        } else {
            showToast(getString(R.string.no_relevant_results_found))
        }
    }

    private fun moveToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(EXTRA_RESULT_TEXT, resultText)
        intent.putExtra(EXTRA_CONFIDENCE_SCORE, confidenceScore)
        currentImageUri?.let { intent.putExtra(EXTRA_IMAGE_URI, it.toString()) }
        startActivity(intent)

        currentImageUri?.let { uri ->
            val asclepius = Asclepius(
                result = resultText,
                confidenceScore = confidenceScore,
                imageUri = uri.toString(),
                date = DateHelper.getCurrentDate()
            )
            mainViewModel.insert(asclepius)
        }
    }

    private fun getNews() {
        mainViewModel.listArticle.observe(this) { news ->
            val filteredNews = news.filter {
                it.title != "[Removed]" && it.description != "[Removed]"
            }

            val adapter = NewsAdapter()
            adapter.onItemClick = { selectedData ->
                val intent = Intent(this, DetailNewsActivity::class.java)
                intent.putExtra(DetailNewsActivity.EXTRA_NEWS, selectedData)
                startActivity(intent)
            }

            adapter.submitList(filteredNews)
            binding.rvNews.adapter = adapter
        }
    }

    private fun isLoading() {
        mainViewModel.isLoading.observe(this) {
            showLoading(it)
        }
    }

    private fun optionMenu() {
        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuHistory -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showProgressIndicator(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun obtainViewModel(activity: AppCompatActivity): MainViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory)[MainViewModel::class.java]
    }
}