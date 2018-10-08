package com.backflippedstudios.crypto_ta.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = [AssetData::class],version = 1)

abstract class AssetDataBase: RoomDatabase(){
    abstract fun assetDataDao(): AssetDataDao

    companion object {
        private var INSTANCE: AssetDataBase? = null

        fun getInstance(context: Context): AssetDataBase?{
            if(INSTANCE == null){
                synchronized(AssetDataBase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            AssetDataBase::class.java, "asset.db")
                            .allowMainThreadQueries()
                            .build()
                }
            }

            return INSTANCE
        }

        fun destroyInstance(){
            INSTANCE = null
        }
    }
}