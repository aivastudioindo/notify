package com.example.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import com.example.data.location.LocationHelper
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ScreenshotHelper {

    private var activeActivityRef: WeakReference<Activity>? = null

    fun registerActivity(activity: Activity) {
        activeActivityRef = WeakReference(activity)
    }

    fun unregisterActivity(activity: Activity) {
        if (activeActivityRef?.get() == activity) {
            activeActivityRef = null
        }
    }

    fun captureScreenshot(context: Context, onResult: (ByteArray?) -> Unit) {
        val activity = activeActivityRef?.get()
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            captureFromActivity(activity) { bitmap ->
                if (bitmap != null) {
                    val bytes = bitmapToByteArray(bitmap)
                    onResult(bytes)
                } else {
                    val fallbackBitmap = generateSystemInfoSnapshotBitmap(context)
                    onResult(bitmapToByteArray(fallbackBitmap))
                }
            }
        } else {
            val systemInfoBitmap = generateSystemInfoSnapshotBitmap(context)
            onResult(bitmapToByteArray(systemInfoBitmap))
        }
    }

    private fun captureFromActivity(activity: Activity, callback: (Bitmap?) -> Unit) {
        val window = activity.window
        if (window == null) {
            callback(null)
            return
        }

        val decorView = window.decorView
        val width = decorView.width
        val height = decorView.height

        if (width <= 0 || height <= 0) {
            callback(null)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val location = IntArray(2)
                decorView.getLocationInWindow(location)

                PixelCopy.request(
                    window,
                    Rect(location[0], location[1], location[0] + width, location[1] + height),
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            callback(bitmap)
                        } else {
                            callback(getBitmapFromView(decorView))
                        }
                    },
                    Handler(Looper.getMainLooper())
                )
            } catch (e: Exception) {
                callback(getBitmapFromView(decorView))
            }
        } else {
            callback(getBitmapFromView(decorView))
        }
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(
                view.width.coerceAtLeast(1),
                view.height.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun generateSystemInfoSnapshotBitmap(context: Context): Bitmap {
        val width = 720
        val height = 1280
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dark background canvas
        val bgPaint = Paint().apply {
            color = Color.parseColor("#12121A")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Header Card Paint
        val cardPaint = Paint().apply {
            color = Color.parseColor("#1E1E2C")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(40f, 60f, (width - 40).toFloat(), 220f, 24f, 24f, cardPaint)

        // Title text
        val titlePaint = Paint().apply {
            color = Color.parseColor("#D0BCFF")
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("📱 FAMLY - TANGKAP LAYAR", 70f, 130f, titlePaint)

        val timeStr = SimpleDateFormat("HH:mm:ss - dd MMMM yyyy", Locale.getDefault()).format(Date())
        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#CAC4D0")
            textSize = 24f
            isAntiAlias = true
        }
        canvas.drawText("Waktu Snapshot: $timeStr", 70f, 180f, subtitlePaint)

        // System status card
        canvas.drawRoundRect(40f, 260f, (width - 40).toFloat(), 1180f, 24f, 24f, cardPaint)

        val labelPaint = Paint().apply {
            color = Color.parseColor("#E6E1E5")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val valPaint = Paint().apply {
            color = Color.parseColor("#A8C7FA")
            textSize = 24f
            isAntiAlias = true
        }

        // Battery level
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batLevel = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1

        var yPos = 330f
        canvas.drawText("⚡ STATUS SISTEM PERANGKAT", 70f, yPos, labelPaint)

        yPos += 70f
        canvas.drawText("• Perangkat: ${Build.MANUFACTURER.uppercase()} ${Build.MODEL}", 70f, yPos, valPaint)

        yPos += 50f
        canvas.drawText("• Versi Android: SDK ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})", 70f, yPos, valPaint)

        yPos += 50f
        val batText = if (batLevel >= 0) "$batLevel%" else "Tidak Terdeteksi"
        canvas.drawText("• Sisa Baterai: $batText", 70f, yPos, valPaint)

        yPos += 70f
        val locationHelper = LocationHelper(context)
        val gpsStatus = if (locationHelper.isGpsEnabled()) "AKTIF 🛰️" else "NONAKTIF ⚠️"
        canvas.drawText("• Status GPS/Lokasi: $gpsStatus", 70f, yPos, valPaint)

        yPos += 70f
        canvas.drawText("ℹ️ TAMPILAN BACKGROUND", 70f, yPos, labelPaint)

        yPos += 60f
        val infoLines = listOf(
            "Layar utama HP anak sedang dalam mode latar",
            "belakang / terkunci. Snapshot status sistem ini",
            "dihasilkan secara otomatis oleh bot Famly."
        )
        for (line in infoLines) {
            canvas.drawText(line, 70f, yPos, valPaint)
            yPos += 45f
        }

        yPos += 60f
        val borderPaint = Paint().apply {
            color = Color.parseColor("#49454F")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(70f, yPos, (width - 70).toFloat(), yPos + 180f, 16f, 16f, borderPaint)

        val footerTitlePaint = Paint().apply {
            color = Color.parseColor("#D0BCFF")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("PERINTAH TELEGRAM AKTIF:", 90f, yPos + 50f, footerTitlePaint)

        val footerTextPaint = Paint().apply {
            color = Color.parseColor("#A8C7FA")
            textSize = 22f
            isAntiAlias = true
        }
        canvas.drawText("/screenshot | /lokasi | /scan | /ping", 90f, yPos + 110f, footerTextPaint)

        return bitmap
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return stream.toByteArray()
    }
}
