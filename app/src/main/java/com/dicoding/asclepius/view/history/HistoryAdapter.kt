package com.dicoding.asclepius.view.history

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.asclepius.data.local.Asclepius
import com.dicoding.asclepius.databinding.ItemRowHistoryBinding
import com.dicoding.asclepius.helper.AsclepiusDiffCallback
import com.dicoding.asclepius.view.result.ResultActivity
import java.util.Locale

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.MyViewHolder>() {
    private val listAsclepius = ArrayList<Asclepius>()

    fun setListAsclepius(listNotes: List<Asclepius>) {
        val diffCallback = AsclepiusDiffCallback(this.listAsclepius, listNotes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.listAsclepius.clear()
        this.listAsclepius.addAll(listNotes)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemRowHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(listAsclepius[position])
    }

    override fun getItemCount(): Int {
        return listAsclepius.size
    }

    inner class MyViewHolder(private val binding: ItemRowHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(asclepius: Asclepius) {
            with(binding) {
                textDate.text = asclepius.date
                textResult.text = asclepius.result
                textConfidenceScore.text = String.format(Locale.getDefault(), "%.2f%%", asclepius.confidenceScore)
                root.setOnClickListener {
                    val intent = Intent(it.context, ResultActivity::class.java)
                    intent.putExtra(ResultActivity.EXTRA_RESULT_TEXT, asclepius.result)
                    intent.putExtra(ResultActivity.EXTRA_CONFIDENCE_SCORE, asclepius.confidenceScore)
                    intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, asclepius.imageUri)
                    it.context.startActivity(intent)
                }
            }
        }
    }
}