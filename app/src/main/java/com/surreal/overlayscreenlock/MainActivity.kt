package com.surreal.overlayscreenlock

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class MainActivity : ComponentActivity() {

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Settings.canDrawOverlays(this)) {
            // Permission granted
            showOverlay()
        } else {
            // Permission not granted
            Toast.makeText(this, "Overlay permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(
                packageName = packageName,
                startOverlay = { startOverlay() },
                hideOverlay = { hideOverlay() },
                isRunning = OverlayService.isRunning
            )
        }
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            // Permission is already granted, proceed with displaying the overlay
            showOverlay()
        }
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun showOverlay() {
        // Start the OverlayService
        val intent = Intent(this, OverlayService::class.java)
        startService(intent)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request notification permission if not granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1
                )
            }
        }
        // Create Notification Channel for Android O and above
        createNotificationChannel()

        // Create a PendingIntent to stop the service when notification is tapped
        val stopSelf = Intent(this, OverlayService::class.java)
        stopSelf.action = "STOP_SERVICE"
        val pStopSelf = PendingIntent.getService(
            this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        // Build the notification
        val builder = NotificationCompat.Builder(this, "overlay_channel_id")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle("Overlay screen lock")
            .setContentText("Tap to turn off")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pStopSelf)
            .setAutoCancel(true)

        // Check for notification permission and send notification
        with(NotificationManagerCompat.from(this)) {
            notify(0, builder.build())
        }

    }

    // Function to create a notification channel for Android O and above
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Overlay Service Channel"
            val descriptionText = "Channel for Overlay Service notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("overlay_channel_id", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun hideOverlay() {
        // Code to display the overlay
        val intent = Intent(this, OverlayService::class.java)
        stopService(intent)
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

}

