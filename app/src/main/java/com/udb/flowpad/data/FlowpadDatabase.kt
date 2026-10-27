package com.udb.flowpad.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        CardEntity::class,
        TaskEntity::class,
        NoteBlockEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FlowpadDatabase : RoomDatabase() {

    abstract fun dao(): FlowpadDao

    companion object {
        private val instances = mutableMapOf<String, FlowpadDatabase>()
        @Synchronized
        fun get(context: Context, uid: String): FlowpadDatabase = instances.getOrPut(uid) {
            val hash = java.security.MessageDigest.getInstance("SHA-256")
                .digest(uid.toByteArray()).joinToString("") { "%02x".format(it) }
            Room.databaseBuilder(context.applicationContext, FlowpadDatabase::class.java,
                "flowpad_$hash.db").build()
        }
    }
}
