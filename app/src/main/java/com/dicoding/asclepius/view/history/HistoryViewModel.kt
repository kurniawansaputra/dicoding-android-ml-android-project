package com.dicoding.asclepius.view.history

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dicoding.asclepius.data.local.Asclepius
import com.dicoding.asclepius.repository.AsclepiusRepository

class HistoryViewModel(application: Application): ViewModel() {
    private val mAsclepiusRepository: AsclepiusRepository = AsclepiusRepository(application)

    fun getAllAsclepius(): LiveData<List<Asclepius>> = mAsclepiusRepository.getAllAsclepius()

    fun deleteAll() = mAsclepiusRepository.deleteAll()
}