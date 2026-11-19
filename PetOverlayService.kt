package com.focuscat.tamil

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.TextView
import android.widget.Toast
import kotlin.math.max

class PetOverlayService : Service() {
    private lateinit var wm: WindowManager
    private var pet: TextView? = null
    private var params: WindowManager.LayoutParams? = null
    private val handler = Handler(Looper.getMainLooper())
    private var moving = true
    private var x = 20
    private var direction = 1
    private var frame = 0
    private val walk = object : Runnable {
        override fun run() {
            val v = pet ?: return
            if (moving) {
                val maxX = max(0, resources.displayMetrics.widthPixels - 130)
                x += 7 * direction
                if (x >= maxX) { x = maxX; direction = -1 }
                if (x <= 0) { x = 0; direction = 1 }
                params?.x = x
                try { wm.updateViewLayout(v, params) } catch (_: Exception) {}
                frame++
                v.text = if (frame % 8 < 4) "🐈" else "🐈‍⬛"
                v.scaleX = if (direction == 1) 1f else -1f
            }
            handler.postDelayed(this, 120)
        }
    }

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createChannel()
        startForeground(7, notification())
        showPet()
        handler.post(walk)
    }

    private fun showPet() {
        if (pet != null) return
        val tv = TextView(this).apply {
            text = "🐈"
            textSize = 42f
            setPadding(8, 4, 8, 4)
            setTextColor(Color.BLACK)
            elevation = 12f
        }
        val type = if (Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 180
        }
        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        tv.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = lp.x; startY = lp.y
                    touchX = event.rawX; touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    lp.x = startX + (event.rawX - touchX).toInt()
                    lp.y = startY + (event.rawY - touchY).toInt()
                    x = lp.x
                    try { wm.updateViewLayout(tv, lp) } catch (_: Exception) {}
                    true
                }
                else -> true
            }
        }
        try {
            wm.addView(tv, lp)
            pet = tv
            params = lp
        } catch (_: Exception) {
            Toast.makeText(this, "திரை மேல் காட்ட அனுமதி தேவை", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("focuscat_pet", "FocusCat கட்டுப்பாடு",
                NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun actionIntent(action: String): PendingIntent {
        val i = Intent(this, PetOverlayService::class.java).setAction(action)
        return PendingIntent.getService(this, action.hashCode(), i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun notification(): Notification {
        val pauseAction = if (moving) ACTION_PAUSE else ACTION_RESUME
        val pauseLabel = if (moving) "⏸ நிறுத்து" else "▶ மீண்டும் நட"
        val builder = if (Build.VERSION.SDK_INT >= 26)
            Notification.Builder(this, "focuscat_pet") else Notification.Builder(this)
        return builder.setContentTitle("🐱 FocusCat தமிழ்")
            .setContentText(if (moving) "பூனை நடந்து கொண்டிருக்கிறது" else "பூனை ஓய்வில் உள்ளது")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, pauseLabel, actionIntent(pauseAction))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "முழுவதும் நிறுத்து", actionIntent(ACTION_STOP))
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> { moving = false; pet?.text = "😴"; updateNotification() }
            ACTION_RESUME -> { moving = true; updateNotification() }
            ACTION_STOP -> { stopSelf(); return START_NOT_STICKY }
        }
        return START_STICKY
    }

    private fun updateNotification() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(7, notification())
    }

    override fun onDestroy() {
        handler.removeCallbacks(walk)
        pet?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        pet = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_PAUSE = "com.focuscat.tamil.PAUSE"
        const val ACTION_RESUME = "com.focuscat.tamil.RESUME"
        const val ACTION_STOP = "com.focuscat.tamil.STOP"
    }
}
