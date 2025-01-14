package com.dicoding.asclepius.view.history

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.asclepius.R
import com.dicoding.asclepius.ViewModelFactory
import com.dicoding.asclepius.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var adapter: HistoryAdapter
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        historyViewModel = obtainViewModel(this)

        optionMenu()
        getHistory()
    }

    private fun getHistory() {
        adapter = HistoryAdapter()

        binding.apply {
            historyViewModel.getAllAsclepius().observe(this@HistoryActivity) { asclepiusList ->
                if (asclepiusList != null && asclepiusList.isNotEmpty()) {
                    adapter.setListAsclepius(asclepiusList)
                    rvHistory.visibility = View.VISIBLE
                    labelNoData.visibility = View.GONE
                } else {
                    rvHistory.visibility = View.GONE
                    labelNoData.visibility = View.VISIBLE
                }
                rvHistory.setHasFixedSize(true)
                rvHistory.adapter = adapter
            }
        }
    }

    private fun optionMenu() {
        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuDeleteAll -> {
                    historyViewModel.deleteAll()
                    true
                }
                else -> false
            }
        }
    }

    private fun obtainViewModel(activity: AppCompatActivity): HistoryViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory)[HistoryViewModel::class.java]
    }
}