package com.videovoid.pro

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.yausername.youtubedl_android.DownloadProgressCallback
import com.yausername.youtubedl_android.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var urlInput: EditText
    private lateinit var qualitySpinner: Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var percentText: TextView
    private lateinit var statusText: TextView
    private lateinit var sourceText: TextView
    private lateinit var heartButton: TextView
    private var maxProgress = 0

    private val qualities = listOf("Best quality", "1080p", "720p", "480p", "Audio only")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                YoutubeDL.getInstance().init(applicationContext)
                FFmpeg.getInstance().init(applicationContext)
                withContext(Dispatchers.Main) {
                    statusText.text = "Ready • Copy a public video link"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = "Download engine initialization failed"
                }
            }
        }
    }

    private fun buildUi() {
        window.statusBarColor = Color.rgb(3, 3, 5)
        window.navigationBarColor = Color.rgb(3, 3, 5)

        val root = FrameLayout(this).apply { setBackgroundColor(Color.rgb(3, 3, 5)) }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
        }
        scroll.addView(content)

        content.addView(TextView(this).apply {
            text = "VIDEOVOID"
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }, lp(-1, -2))

        content.addView(TextView(this).apply {
            text = "PRO DOWNLOADER"
            textSize = 11f
            letterSpacing = 0.28f
            setTextColor(Color.rgb(145, 145, 155))
            gravity = Gravity.CENTER
        }, lp(-1, 28))

        val hole = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(250), dp(250)).apply {
                topMargin = dp(18); bottomMargin = dp(16)
            }
        }

        hole.addView(TextView(this).apply {
            text = "◎"
            textSize = 220f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(17, 17, 23))
        }, FrameLayout.LayoutParams(-1, -1))

        heartButton = TextView(this).apply {
            text = "♥"
            textSize = 43f
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_heart)
            elevation = dp(12).toFloat()
            setOnClickListener { onHeartPressed() }
        }
        hole.addView(heartButton, FrameLayout.LayoutParams(dp(120), dp(120), Gravity.CENTER))
        content.addView(hole)

        sourceText = TextView(this).apply {
            text = "SOURCE • WAITING"
            textSize = 11f
            letterSpacing = 0.16f
            setTextColor(Color.rgb(160, 160, 170))
            gravity = Gravity.CENTER
        }
        content.addView(sourceText, lp(-1, 30))

        urlInput = EditText(this).apply {
            hint = "Paste video URL"
            hintTextColor = Color.rgb(105, 105, 115)
            setTextColor(Color.WHITE)
            textSize = 14f
            singleLine = true
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_input)
        }
        content.addView(urlInput, LinearLayout.LayoutParams(-1, dp(56)).apply { topMargin = dp(4) })

        content.addView(TextView(this).apply {
            text = "VIDEO QUALITY"
            textSize = 10f
            letterSpacing = 0.15f
            setTextColor(Color.rgb(150, 150, 160))
            setPadding(dp(4), dp(20), 0, dp(7))
        }, lp(-1, 40))

        qualitySpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                qualities
            )
            setBackgroundResource(R.drawable.bg_chip)
        }
        content.addView(qualitySpinner, lp(-1, 50))

        content.addView(TextView(this).apply {
            text = "DOWNLOAD PROGRESS"
            textSize = 10f
            letterSpacing = 0.15f
            setTextColor(Color.rgb(150, 150, 160))
            setPadding(dp(4), dp(18), 0, dp(7))
        }, lp(-1, 40))

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            progressDrawable = ContextCompat.getDrawable(this@MainActivity, R.drawable.progress_bg)
        }
        content.addView(progressBar, lp(-1, 9))

        percentText = TextView(this).apply {
            text = "0%"
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        content.addView(percentText, lp(-1, 32))

        statusText = TextView(this).apply {
            text = "Initializing download engine…"
            textSize = 12f
            setTextColor(Color.rgb(155, 155, 165))
            gravity = Gravity.CENTER
        }
        content.addView(statusText, lp(-1, 45))

        content.addView(TextView(this).apply {
            text = "Public links supported by the current yt-dlp extractors. Private, DRM-protected or unavailable media may fail."
            textSize = 10f
            setTextColor(Color.rgb(90, 90, 100))
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(10), dp(8), 0)
        }, lp(-1, 60))

        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
    }

    private fun onHeartPressed() {
        heartButton.animate().scaleX(0.86f).scaleY(0.86f).setDuration(80).withEndAction {
            heartButton.animate().scaleX(1.08f).scaleY(1.08f).setDuration(90).withEndAction {
                heartButton.animate().scaleX(1f).scaleY(1f).setDuration(90).start()
            }.start()
        }.start()

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData? = clipboard.primaryClip
        val copied = if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).coerceToText(this).toString()
        } else ""

        if (isHttpUrl(copied)) {
            urlInput.setText(copied)
            urlInput.setSelection(urlInput.text.length)
        }

        val url = urlInput.text.toString().trim()
        if (!isHttpUrl(url)) {
            statusText.text = "Copy a public video URL, then tap the heart"
            return
        }

        sourceText.text = "SOURCE • ${detectSource(url)}"
        startDownload(url, qualitySpinner.selectedItemPosition)
    }

    private fun startDownload(url: String, quality: Int) {
        maxProgress = 0
        progressBar.progress = 0
        percentText.text = "0%"
        statusText.text = "Preparing download…"

        lifecycleScope.launch(Dispatchers.IO) {
            val workDir = File(externalCacheDir, "videovoid").apply { mkdirs() }
            workDir.listFiles()?.forEach { it.delete() }

            try {
                val request = YoutubeDLRequest(url)
                request.addOption("--no-mtime")
                request.addOption("--newline")
                request.addOption("--no-playlist")
                request.addOption("-o", File(workDir, "%(title).100s.%(ext)s").absolutePath)

                val format = when (quality) {
                    1 -> "bestvideo[height<=1080][ext=mp4]+bestaudio[ext=m4a]/best[height<=1080][ext=mp4]/best"
                    2 -> "bestvideo[height<=720][ext=mp4]+bestaudio[ext=m4a]/best[height<=720][ext=mp4]/best"
                    3 -> "bestvideo[height<=480][ext=mp4]+bestaudio[ext=m4a]/best[height<=480][ext=mp4]/best"
                    4 -> "bestaudio/best"
                    else -> "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best"
                }
                request.addOption("-f", format)

                val callback = DownloadProgressCallback { progress, _ ->
                    val p = progress.toInt().coerceIn(0, 100)
                    if (p > maxProgress) {
                        maxProgress = p
                        runOnUiThread {
                            progressBar.progress = maxProgress
                            percentText.text = "$maxProgress%"
                            statusText.text = if (maxProgress < 100) "Downloading…" else "Finalizing…"
                        }
                    }
                }

                YoutubeDL.getInstance().execute(request, callback)

                val output = workDir.listFiles()
                    ?.filter { it.isFile && !it.name.endsWith(".part") && !it.name.endsWith(".ytdl") }
                    ?.maxByOrNull { it.lastModified() }
                    ?: error("No downloaded media file was produced")

                saveToDownloads(output)

                withContext(Dispatchers.Main) {
                    progressBar.progress = 100
                    percentText.text = "100%"
                    statusText.text = "Downloaded to Download/VideoVoid"
                    Toast.makeText(this@MainActivity, "Download complete", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = "Download failed: ${e.message ?: "unknown error"}"
                    Toast.makeText(this@MainActivity, "Download failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveToDownloads(source: File) {
        if (Build.VERSION.SDK_INT >= 29) {
            val mime = if (source.name.endsWith(".mp3", true) || source.name.endsWith(".m4a", true)) {
                "audio/*"
            } else "video/*"

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, source.name)
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/VideoVoid")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Could not create Downloads file")

            try {
                contentResolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(source).use { input -> input.copyTo(out) }
                } ?: error("Could not open output stream")

                contentResolver.update(
                    uri,
                    ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                    null,
                    null
                )
            } catch (e: Exception) {
                contentResolver.delete(uri, null, null)
                throw e
            }
        } else {
            @Suppress("DEPRECATION")
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "VideoVoid"
            )
            if (!dir.exists()) dir.mkdirs()
            FileInputStream(source).use { input ->
                java.io.FileOutputStream(File(dir, source.name)).use { out -> input.copyTo(out) }
            }
        }
    }

    private fun detectSource(url: String): String {
        val host = Uri.parse(url).host?.lowercase(Locale.US) ?: return "UNKNOWN"
        return when {
            "youtube.com" in host || "youtu.be" in host -> "YOUTUBE"
            "instagram.com" in host -> "INSTAGRAM"
            "facebook.com" in host || "fb.watch" in host -> "FACEBOOK"
            "tiktok.com" in host -> "TIKTOK"
            "twitter.com" in host || "x.com" in host -> "X"
            "vimeo.com" in host -> "VIMEO"
            else -> host.removePrefix("www.").uppercase(Locale.US)
        }
    }

    private fun isHttpUrl(text: String) =
        text.startsWith("https://") || text.startsWith("http://")

    private fun dp(value: Int) =
        (value * resources.displayMetrics.density).toInt()

    private fun lp(w: Int, h: Int) =
        LinearLayout.LayoutParams(if (w == -1) -1 else dp(w), dp(h))
}
