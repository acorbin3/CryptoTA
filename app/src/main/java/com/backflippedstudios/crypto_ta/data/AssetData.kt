package com.backflippedstudios.crypto_ta.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "assetData")
data class AssetData(@PrimaryKey(autoGenerate = true) var id: Long?,
                     @ColumnInfo(name = "asset") var asset: String){
    constructor():this(null,"")
}