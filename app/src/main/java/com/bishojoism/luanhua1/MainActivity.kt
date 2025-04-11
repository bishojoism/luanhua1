@file:OptIn(ExperimentalMaterial3Api::class)

package com.bishojoism.luanhua1

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.Intent.CATEGORY_OPENABLE
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arthenica.ffmpegkit.FFmpegSession
import com.bishojoism.luanhua1.ui.theme.Luanhua1Theme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Luanhua1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var seed by remember { mutableStateOf("") }
    var session by remember { mutableStateOf<FFmpegSession?>(null) }
    val encryption = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let {
                val input = File(context.cacheDir, "input.mp4")
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    FileOutputStream(input).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                session = jiami(context, seed) {
                    session = null
                }
            }
        }
    }
    val decryption = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let {
                val input = File(context.cacheDir, "input.mp4")
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    FileOutputStream(input).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                session = jiemi(context, seed) {
                    session = null
                }
            }
        }
    }
    Column(modifier) {
        TextField(
            value = seed,
            onValueChange = { seed = it },
            label = { Text("密码") }
        )
        Row {
            Button(onClick = {
                encryption.launch(Intent(ACTION_GET_CONTENT).apply {
                    type = "video/mp4"
                    addCategory(CATEGORY_OPENABLE)
                })
            }) {
                Text("加密本地视频")
            }
            Button(onClick = {
                decryption.launch(Intent(ACTION_GET_CONTENT).apply {
                    type = "video/mp4"
                    addCategory(CATEGORY_OPENABLE)
                })
            }) {
                Text("解密本地视频")
            }
        }
    }
    if (session != null) {
        BasicAlertDialog(
            onDismissRequest = {}
        ) {
            Surface(
                modifier = Modifier.wrapContentSize(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "正在处理视频……"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(
                        onClick = { session?.cancel() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("取消")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Luanhua1Theme {
        Greeting()
    }
}