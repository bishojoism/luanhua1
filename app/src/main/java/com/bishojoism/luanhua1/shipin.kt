package com.bishojoism.luanhua1

import android.content.ContentValues
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.io.FileInputStream

fun jiami(context: Context, seed: String, callback: () -> Unit): FFmpegSession {
    return FFmpegKit.executeWithArgumentsAsync(
        arrayOf(
            "-i",
            File(context.cacheDir, "input.mp4").path,
            "-filter_complex",
            encrypt(seed),
            File(context.cacheDir, "output.mp4").path,
            "-y"
        )
    ) {
        if (ReturnCode.isSuccess(it.returnCode)) {
            context.contentResolver.insert(
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, "加密过的视频")
                    put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
                }
            )?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    FileInputStream(File(context.cacheDir, "output.mp4")).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
            Handler(Looper.getMainLooper()).post {
                Toast
                    .makeText(context, "已将加密过的视频保存至下载目录", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (ReturnCode.isCancel(it.returnCode)) {
            Handler(Looper.getMainLooper()).post {
                Toast
                    .makeText(context, "加密已被用户取消", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Handler(Looper.getMainLooper()).post {
                Toast
                    .makeText(context, "加密失败：${it.failStackTrace}", Toast.LENGTH_LONG)
                    .show()
            }
        }
        callback()
    }
}

fun jiemi(context: Context, seed: String, callback: () -> Unit): FFmpegSession {
    return FFmpegKit.executeWithArgumentsAsync(
        arrayOf(
            "-i",
            File(context.cacheDir, "input.mp4").path,
            "-filter_complex",
            decrypt(seed),
            File(context.cacheDir, "output.mp4").path,
            "-y"
        )
    ) {
        if (ReturnCode.isSuccess(it.returnCode)) {
            context.contentResolver.insert(
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, "解密过的视频")
                    put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
                }
            )?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    FileInputStream(File(context.cacheDir, "output.mp4")).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
            Handler(Looper.getMainLooper()).post {
                Toast
                    .makeText(context, "已将解密过的视频保存至下载目录", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (ReturnCode.isCancel(it.returnCode)) {
            Handler(Looper.getMainLooper()).post {
                Toast
                    .makeText(context, "解密已被用户取消", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Handler(Looper.getMainLooper()).post {
                Toast
                    .makeText(context, "解密失败：${it.failStackTrace}", Toast.LENGTH_LONG)
                    .show()
            }
        }
        callback()
    }
}