package com.backflippedstudios.crypto_ta.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface AssetDataDao{
    @Query("SELECT * from assetData")
    fun getAll(): List<AssetData>

    @Insert(onConflict = REPLACE)
    fun insert(assetData: AssetData)

    @Query("DELETE from assetData")
    fun deleteALL()

    @Query("SELECT COUNT(*) from assetData")
    fun count(): Int
}