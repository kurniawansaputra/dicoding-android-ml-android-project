package com.dicoding.asclepius.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AsclepiusDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(asclepius: Asclepius)

    @Update
    fun update(asclepius: Asclepius)

    @Delete
    fun delete(asclepius: Asclepius)

    @Query("SELECT * from asclepius ORDER BY id ASC")
    fun getAllAsclepius(): LiveData<List<Asclepius>>

    @Query("DELETE FROM asclepius")
    fun deleteAll()
}