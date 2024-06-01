package com.surreal.overlayscreenlock

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.surreal.overlayscreenlock.ui.theme.OverlayScreenLockTheme


@Composable
fun MainScreen(
    packageName: String,
    startOverlay: () -> Unit,
    hideOverlay: () -> Unit,
    isRunning: Boolean
) {
    val local = LocalContext.current
    OverlayScreenLockTheme {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            var startOrStop: Boolean by remember { mutableStateOf(!isRunning) }
            Column(Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.padding(top=20.dp))
                Text(modifier = Modifier.padding(start = 15.dp),text = """
                    Для запуска необходимо дать разрешение отображение приложения поверх других окон
                    
                    Передвигайте оверлэй зажимая темную часть
                """.trimIndent())
                HorizontalDivider(Modifier.padding(vertical = 15.dp),thickness = 3.dp, color = Color.Black)
                ColumnItem(text = "Разрешение на виджет", btnText = "Разрешить") {
                    if (!Settings.canDrawOverlays(local)) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivityForResult(local.getActivity()!!, intent, 100, null)

                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 15.dp),thickness = 3.dp, color = Color.Black)
                ColumnItem(text = "Разрешение на Уведомления", btnText = "Разрешить") {
                    val permissionState = ContextCompat.checkSelfPermission(
                        local,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    // If the permission is not granted, request it.
                    if (permissionState == PackageManager.PERMISSION_DENIED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ActivityCompat.requestPermissions(
                                local.getActivity()!!,
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                1
                            )
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 15.dp),thickness = 3.dp, color = Color.Black)
                ColumnItem(text = "Запуск Виджета", btnText = "Старт") {
                    if (startOrStop) {
                        startOverlay()
                        startOrStop = false
                    } else {
                        hideOverlay()
                        startOrStop = true
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    MainScreen(
        packageName = "",
        startOverlay = { /*TODO*/ },
        hideOverlay = { /*TODO*/ },
        isRunning = false
    )
}

@Composable
fun ColumnItem(text:String, btnText:String, onClick:()->Unit){
    Text(modifier=Modifier.padding(start = 15.dp),text = text)
    Button(modifier=Modifier.padding(start = 25.dp),onClick = onClick) {
        Text(text = btnText)
    }
}
fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
