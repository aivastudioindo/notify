package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class ScreenCaptureService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("RESULT_CODE", -1) ?: -1
        val resultData = intent?.getParcelableExtra<Intent>("RESULT_DATA")

        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (resultCode != -1 && resultData != null) {
            val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)
            Log.d("ScreenCaptureService", "MediaProjection berhasil diinisialisasi!")
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Layanan Pemantauan Layar Famly",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Layanan latar belakang untuk pemantauan perlindungan keluarga."
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Famly Perlindungan Aktif")
            .setContentText("Sistem pemantauan keselamatan anak berjalan.")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "FamlyScreenCaptureChannel"
        private const val NOTIFICATION_ID = 9002

        var mediaProjection: MediaProjection? = null
            private set

        fun isProjectionReady(): Boolean = mediaProjection != null

        fun captureRealTimeScreen(context: Context, callback: (Bitmap?) -> Unit) {
            val projection = mediaProjection
            if (projection == null) {
                callback(null)
                return
            }

            try {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val metrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                wm.defaultDisplay.getRealMetrics(metrics)

                val width = metrics.widthPixels
                val height = metrics.heightPixels
                val density = metrics.densityDpi

                val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
                val virtualDisplay: VirtualDisplay? = projection.createVirtualDisplay(
                    "FamlyScreenCapture",
                    width,
                    height,
                    density,
                    DisplayMetrics.DENSITY_DEFAULT,
                    imageReader.surface,
                    null,
                    Handler(Looper.getMainLooper())
                )

                Handler(Looper.getMainLooper()).postDelayed({
                    var bitmap: Bitmap? = null
                    var image: Image? = null
                    try {
                        image = imageReader.acquireLatestImage()
                        if (image != null) {
                            val planes = image.planes
                            val buffer = planes[0].buffer
                            val pixelStride = planes[0].pixelStride
                            val rowStride = planes[0].rowStride
                            val rowPadding = rowStride - pixelStride * width

                            val tempBitmap = Bitmap.createBitmap(
                                width + rowPadding / pixelStride,
                                height,
                                Bitmap.Config.ARGB_8888
                            )
                            tempBitmap.copyPixelsFromBuffer(buffer)
                            bitmap = Bitmap.createBitmap(tempBitmap, 0, 0, width, height)
                        }
                    } catch (e: Exception) {
                        Log.e("ScreenCaptureService", "Error capture image: ${e.message}", e)
                    } finally {
                        image?.close()
                        virtualDisplay?.release()
                        imageReader.close()
                        callback(bitmap)
                    }
                }, 300)
            } catch (e: Exception) {
                Log.e("ScreenCaptureService", "Error virtual display: ${e.message}", e)
                callback(null)
            }
        }
    }
}
