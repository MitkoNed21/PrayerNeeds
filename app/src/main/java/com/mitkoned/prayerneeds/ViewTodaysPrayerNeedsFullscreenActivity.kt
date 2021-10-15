package com.mitkoned.prayerneeds

import androidx.appcompat.app.AppCompatActivity
import android.app.ActionBar
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat.getDisplay
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ViewTodaysPrayerNeedsFullscreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.title = getString(R.string.prayerGatheringText)

        val appBarLayout = LayoutInflater.from(this).inflate(R.layout.all_prayer_needs_top_bar_layout, null)
        supportActionBar?.apply {
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)

            //setBackgroundDrawable(ColorDrawable(Color.parseColor("#ff080808")))
            displayOptions = displayOptions.or(ActionBar.DISPLAY_SHOW_CUSTOM)

            appBarLayout.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            customView = appBarLayout
        }

        appBarLayout.findViewById<TextView>(R.id.tv_currentDate).apply {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.US)
            text = dateFormat.format(Calendar.getInstance().time)
        }

        try {
            val namesAddedToday = intent.getStringArrayExtra("namesAddedToday")!!

            val fileNames = namesAddedToday.map { "${filesDir.path}/$it.json" }

            val rvPrayerNeeds = this.findViewById<RecyclerView>(R.id.rv_prayerNeeds)
            val adapter = AllTodayPrayerNeedsElementAdapter(fileNames)
            rvPrayerNeeds.adapter = adapter
            rvPrayerNeeds.layoutManager = LinearLayoutManager(this)
        } catch (e: Exception) {
            Toast.makeText(this, "TEST", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, e.stackTrace.joinToString { "\n" }, Toast.LENGTH_LONG).show()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cutout = getDisplay(window.decorView)?.cutout

            if (cutout != null) {
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }
    }
}