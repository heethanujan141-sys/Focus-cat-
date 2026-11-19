package com.focuscat.tamil

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 36)
            setBackgroundColor(Color.rgb(255, 245, 250))
        }
        val title = TextView(this).apply {
            text = "🐱 FocusCat தமிழ்"
            textSize = 30f
            setTextColor(Color.rgb(90, 55, 105))
            gravity = Gravity.CENTER
        }
        val desc = TextView(this).apply {
            text = "உன் போன் திரையில் குட்டிப் பூனை நடந்து திரியும்!\n\nமற்ற செயலிகளின் மேலேயும் காட்ட அனுமதி தேவை."
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
            setTextColor(Color.DKGRAY)
        }
        val start = Button(this).apply {
            text = "🐾 பூனையைத் தொடங்கு"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))
                    startActivity(i)
                } else {
                    startForegroundService(Intent(this@MainActivity, PetOverlayService::class.java))
                }
            }
        }
        val help = TextView(this).apply {
            text = "நிறுத்த / மீண்டும் தொடங்க: மேலிருந்து அறிவிப்பு பட்டையை இழுத்து FocusCat கட்டுப்பாடுகளைத் தேர்வு செய்.\n\nபூனையை விரலால் இழுத்து இடம் மாற்றலாம்."
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
            setTextColor(Color.DKGRAY)
        }
        layout.addView(title, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layout.addView(desc)
        layout.addView(start)
        layout.addView(help)
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        // Returning from overlay permission settings: user can tap Start.
    }
}
