package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.NotificationRepository
import com.example.data.security.EncryptionManager

class NotifVaultApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val encryptionManager by lazy { EncryptionManager.getInstance(this) }
    val repository by lazy {
        NotificationRepository(database.notificationDao(), encryptionManager)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: NotifVaultApplication
            private set
    }
}
