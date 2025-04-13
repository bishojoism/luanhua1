@file:OptIn(ExperimentalMaterial3Api::class)

package com.bishojoism.luanhua1

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.Intent.CATEGORY_OPENABLE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
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
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var seed by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var session by remember { mutableStateOf<FFmpegSession?>(null) }
    var getting by remember { mutableStateOf(false) }
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
            modifier = Modifier.fillMaxWidth(),
            value = seed,
            onValueChange = { seed = it },
            label = { Text("密码") }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                encryption.launch(Intent(ACTION_GET_CONTENT).apply {
                    type = "video/mp4"
                    addCategory(CATEGORY_OPENABLE)
                })
            }) {
                Text("加载并加密")
            }
            TextButton(onClick = {
                decryption.launch(Intent(ACTION_GET_CONTENT).apply {
                    type = "video/mp4"
                    addCategory(CATEGORY_OPENABLE)
                })
            }) {
                Text("加载并解密")
            }
        }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = url,
            onValueChange = { url = it },
            label = { Text("网址") },
            trailingIcon = {
                Row {
                    TextButton(onClick = {
                        getting = true
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            loadUrl(
                                "https://jx.xmflv.com/?url=${
                                    URLEncoder.encode(
                                        url,
                                        StandardCharsets.UTF_8
                                    )
                                }"
                            )
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (!getting) return@postDelayed
                                evaluateJavascript("document.querySelector('video').src") { url ->
                                    if (!getting) return@evaluateJavascript
                                    if (url == "null") {
                                        Toast.makeText(
                                            context,
                                            "寻找视频失败……",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        getting = false
                                        return@evaluateJavascript
                                    }
                                    Thread {
                                        val response = OkHttpClient()
                                            .newCall(
                                                Request
                                                    .Builder()
                                                    .url(url.substring(1 until url.length - 1))
                                                    .build()
                                            )
                                            .execute()
                                        if (!getting) return@Thread
                                        response
                                            .body
                                            ?.byteStream()
                                            ?.use { input ->
                                                File(context.cacheDir, "input.mp4")
                                                    .outputStream()
                                                    .use { output ->
                                                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                                                        var bytes = input.read(buffer)
                                                        if (!getting) return@Thread
                                                        while (bytes >= 0) {
                                                            output.write(buffer, 0, bytes)
                                                            if (!getting) return@Thread
                                                            bytes = input.read(buffer)
                                                            if (!getting) return@Thread
                                                        }
                                                        getting = false
                                                        session = jiami(context, seed) {
                                                            session = null
                                                        }
                                                    }
                                            }
                                    }.start()
                                }
                            }, 3000)
                        }
                    }) {
                        Text("解析并加密")
                    }
                    TextButton(onClick = {
                        getting = true
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            loadUrl(
                                "https://jx.xmflv.com/?url=${
                                    URLEncoder.encode(
                                        url,
                                        StandardCharsets.UTF_8
                                    )
                                }"
                            )
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (!getting) return@postDelayed
                                evaluateJavascript("document.querySelector('video').src") { url ->
                                    if (!getting) return@evaluateJavascript
                                    if (url == "null") {
                                        Toast.makeText(
                                            context,
                                            "寻找视频失败……",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        getting = false
                                        return@evaluateJavascript
                                    }
                                    Thread {
                                        val response = OkHttpClient()
                                            .newCall(
                                                Request
                                                    .Builder()
                                                    .url(url.substring(1 until url.length - 1))
                                                    .build()
                                            )
                                            .execute()
                                        if (!getting) return@Thread
                                        response
                                            .body
                                            ?.byteStream()
                                            ?.use { input ->
                                                File(context.cacheDir, "input.mp4")
                                                    .outputStream()
                                                    .use { output ->
                                                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                                                        var bytes = input.read(buffer)
                                                        if (!getting) return@Thread
                                                        while (bytes >= 0) {
                                                            output.write(buffer, 0, bytes)
                                                            if (!getting) return@Thread
                                                            bytes = input.read(buffer)
                                                            if (!getting) return@Thread
                                                        }
                                                        getting = false
                                                        session = jiemi(context, seed) {
                                                            session = null
                                                        }
                                                    }
                                            }
                                    }.start()
                                }
                            }, 3000)
                        }
                    }) {
                        Text("解析并解密")
                    }
                }
            }
        )
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
    if (getting) {
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
                        text = "正在获取视频……"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(
                        onClick = { getting = false },
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