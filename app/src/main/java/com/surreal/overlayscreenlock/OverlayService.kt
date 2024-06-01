package com.surreal.overlayscreenlock

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout

class OverlayService : Service() {

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var blackOverlay: FrameLayout
    private var isBlackOverlayVisible = false

    companion object {
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.LEFT
        params.x = 0
        params.y = 0

        // Full-screen black overlay
        // Full-screen black overlay
        blackOverlay = FrameLayout(this)
        blackOverlay.setBackgroundColor(0xFF000000.toInt())
        blackOverlay.layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        blackOverlay.visibility = View.GONE
        // Turn off button
        overlayView.findViewById<Button>(R.id.overlay_content).setOnClickListener {
            toggleBlackOverlay()
        }

        overlayView.findViewById<View>(R.id.overlay)
            .setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(view: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            Log.d("LOG", "aaa")
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            Log.d("LOG", "a")
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager.updateViewLayout(overlayView, params)
                            return true
                        }
                    }
                    return false
                }
            })

        windowManager.addView(blackOverlay, blackOverlay.layoutParams)

        windowManager.addView(overlayView, params)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        if (::overlayView.isInitialized) windowManager.removeView(overlayView)
        if (::blackOverlay.isInitialized && isBlackOverlayVisible) windowManager.removeView(blackOverlay)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun toggleBlackOverlay() {
        if (isBlackOverlayVisible) {
            blackOverlay.visibility = View.GONE
            isBlackOverlayVisible = false
        } else {
            blackOverlay.visibility = View.VISIBLE
            isBlackOverlayVisible = true
        }
    }
}
