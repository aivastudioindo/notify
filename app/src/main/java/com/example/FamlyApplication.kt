package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.NotificationRepository
import com.example.data.security.EncryptionManager
import com.example.data.telegram.TelegramBotManager

import com.example.service.FamlyForegroundService

class FamlyApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val encryptionManager by lazy { EncryptionManager.getInstance(this) }
    val telegramBotManager by lazy { TelegramBotManager.getInstance(this) }
    val appFilterManager by lazy { com.example.data.filter.AppFilterManager.getInstance(this) }
    val repository by lazy {
        NotificationRepository(database.notificationDao(), encryptionManager, telegramBotManager)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        FamlyForegroundService.startService(this)
    }

    companion object {
        lateinit var instance: FamlyApplication
            private set
    }
}
