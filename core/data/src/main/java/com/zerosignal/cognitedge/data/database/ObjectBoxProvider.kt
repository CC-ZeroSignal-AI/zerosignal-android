package com.zerosignal.cognitedge.data.database

import android.content.Context
import com.zerosignal.cognitedge.data.database.entity.MyObjectBox
import io.objectbox.BoxStore

object ObjectBoxProvider {

    @Volatile
    private var boxStore: BoxStore? = null

    fun get(context: Context): BoxStore {
        return boxStore ?: synchronized(this) {
            boxStore ?: buildBoxStore(context).also { boxStore = it }
        }
    }

    private fun buildBoxStore(context: Context): BoxStore {
        return MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
    }
}
