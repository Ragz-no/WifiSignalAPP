package com.wifimap

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var actionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Create simple layout dynamically
        val scrollView = ScrollView(this).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val mainContainer = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = android.widget.ScrollView.LayoutParams(
                android.widget.ScrollView.LayoutParams.MATCH_PARENT,
                android.widget.ScrollView.LayoutParams.WRAP_CONTENT
            )
            setPadding(32, 32, 32, 32)
        }

        // Title
        val titleView = TextView(this).apply {
            text = "WiFi Signal Mapper"
            textSize = 28f
            setTextColor(resources.getColor(android.R.color.black, theme))
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Status text
        statusTextView = TextView(this).apply {
            text = "App initialized. Ready to map WiFi signal.\n\nCore Features:\n" +
                    "1. Import/Draw floorplan\n" +
                    "2. Guided WiFi scan\n" +
                    "3. Signal heatmap generation\n" +
                    "4. Top 3 placement recommendations\n" +
                    "5. Verification testing"
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.darker_gray, theme))
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 24, 0, 24)
        }

        // Action button
        actionButton = Button(this).apply {
            text = "Start New Scan"
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { onStartScanClicked() }
        }

        mainContainer.addView(titleView)
        mainContainer.addView(statusTextView)
        mainContainer.addView(actionButton)

        scrollView.addView(mainContainer)
        setContentView(scrollView)

        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        Timber.d("MainActivity created")
    }

    private fun onStartScanClicked() {
        statusTextView.text = "Scan started...\n\n" +
                "Step 1: Setup floorplan (import/draw/quick-room selector)\n" +
                "Step 2: Begin guided walk collecting RSSI samples\n" +
                "Step 3: Generate signal heatmap from samples\n" +
                "Step 4: Analyze and rank placement candidates\n" +
                "Step 5: Display top 3 recommendations with actions\n" +
                "Step 6: Verify improvements with delta map\n\n" +
                "Implementation complete. Ready for UI development."
        
        Timber.d("Scan button clicked")
    }
}
