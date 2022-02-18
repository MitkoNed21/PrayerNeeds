package com.mitkoned.prayerneeds.activities

import androidx.appcompat.app.AppCompatActivity
import android.app.ActionBar
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat.getDisplay
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mitkoned.prayerneeds.adapters.AllTodayPrayerNeedsElementAdapter
import com.mitkoned.prayerneeds.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ViewTodaysPrayerNeedsFullscreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_todays_prayer_needs_fullscreen)

        this.title = getString(R.string.prayerGatheringText)

        val appBarLayout = LayoutInflater.from(this).inflate(R.layout.layout__top_bar_all_prayer_needs, null)
        supportActionBar?.apply {
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)

            //setBackgroundDrawable(ColorDrawable(Color.parseColor("#ff080808")))
            //displayOptions = displayOptions.or(ActionBar.DISPLAY_SHOW_CUSTOM)

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

            val rvPrayerNeeds = this.findViewById<RecyclerView>(R.id.rv_prayerNeedsToday)
            val adapter = AllTodayPrayerNeedsElementAdapter(fileNames)
            rvPrayerNeeds.adapter = adapter
            rvPrayerNeeds.layoutManager = LinearLayoutManager(this)

            val displaySize: Point
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val insets = windowManager.currentWindowMetrics.windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.navigationBars() or
                    WindowInsets.Type.displayCutout()
                )

                val insetsWidth = insets.left + insets.right
                val insetsHeight = insets.bottom + insets.top
                val bounds = windowManager.currentWindowMetrics.bounds

                displaySize = Point(bounds.width() - insetsWidth, bounds.height() - insetsHeight)
            } else {
                displaySize = Point()
                windowManager.defaultDisplay.getSize(displaySize)
            }

            Handler(Looper.getMainLooper()).post {
                // add a small buffer in case of a few pixels inaccuracy
                if (rvPrayerNeeds.computeVerticalScrollRange() > displaySize.y - appBarLayout.height - 10) {
                    rvPrayerNeeds.updatePadding(bottom = displaySize.y)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
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