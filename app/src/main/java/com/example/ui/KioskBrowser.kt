package com.example.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.MediaStore
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.MimeTypeMap
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.data.AppConfigRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.MalayGold
import com.example.ui.theme.MalayGoldLight
import com.example.ui.theme.ToskaAccent
import com.example.ui.theme.ToskaBright
import com.example.ui.theme.ToskaDark
import com.example.ui.theme.ToskaDarkMedium
import com.example.ui.theme.ToskaTeal
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URLDecoder

const val TARGET_URL = "https://share.gemini.google/MgVLfdtGxua0"
const val LOGIN_URL = "https://accounts.google.com/signin?"

private const val PREFS_NAME = "kiosk_browser_settings"
private const val KEY_TOP_EXPANSION = "pref_top_expansion_percent"
private const val KEY_BOTTOM_EXPANSION = "pref_bottom_expansion_percent"
private const val MIN_TOP_EXPANSION = 1f
private const val MAX_TOP_EXPANSION = 25f
private const val MIN_BOTTOM_EXPANSION = 8f
private const val MAX_BOTTOM_EXPANSION = 40f
private const val DEFAULT_TOP_EXPANSION = 4f
private const val DEFAULT_BOTTOM_EXPANSION = 21f

// Global callback listener to trigger the rich modern download notification banner
internal var onDownloadFinishedListener: ((DownloadSuccessEvent) -> Unit)? = null

/**
 * Checks whether the URL indicates that Google authentication is completely finished
 * and user has landed on myaccount.google.com, which should navigate back to Gemini.
 */
fun isPostLoginUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    val lower = url.lowercase()
    
    // If it's already on Gemini or share URL, it is NOT a pending post-login redirect
    if (lower.contains("gemini.google") || lower.contains("share.gemini.google")) return false
    
    // Do NOT intercept active sign-in flows
    if (lower.contains("signin") || lower.contains("identifier") || lower.contains("challenge") || lower.contains("v3/signin")) {
        return false
    }

    // Only redirect if specifically landed on Google Account dashboard post-login
    return lower.contains("myaccount.google.com")
}

/**
 * Data class representing a pending download task awaiting storage permissions.
 */
data class PendingDownloadTask(
    val url: String,
    val userAgent: String,
    val contentDisposition: String,
    val mimetype: String
)

/**
 * Returns required permissions for downloading files and receiving download notifications.
 */
fun getRequiredDownloadPermissions(): Array<String> {
    val permissions = mutableListOf<String>()
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
    }
    return permissions.toTypedArray()
}

/**
 * Checks whether the current URL is the target Gemini Canvas page
 * rather than an authentication / Google Sign-In flow.
 */
fun isGeminiCanvasUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    val lower = url.lowercase()
    val isAuthFlow = lower.contains("accounts.google.com") ||
            lower.contains("signin") ||
            lower.contains("servicelogin") ||
            lower.contains("oauth") ||
            lower.contains("identifier") ||
            lower.contains("challenge") ||
            lower.contains("v3/signin")
    if (isAuthFlow) return false
    return lower.contains("share.gemini.google") || lower.contains("gemini.google.com")
}

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun KioskBrowserScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appConfigRepo = remember(context) { AppConfigRepository(context) }
    val sharedPrefs = remember(context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentTargetUrl by rememberSaveable {
        mutableStateOf(appConfigRepo.getCachedTargetUrl(TARGET_URL))
    }
    var currentUrl by remember { mutableStateOf<String?>(currentTargetUrl) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loadingProgress by remember { mutableIntStateOf(0) }

    // Side Admin Drawer states (Minimalist hidden side menu)
    var isSideMenuOpen by rememberSaveable { mutableStateOf(false) }
    var isAdminLoggedIn by rememberSaveable { mutableStateOf(false) }
    var adminPasswordInput by rememberSaveable { mutableStateOf("") }
    var adminPasswordError by rememberSaveable { mutableStateOf<String?>(null) }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var newTargetUrlInput by rememberSaveable { mutableStateOf("") }
    var isSavingUrl by remember { mutableStateOf(false) }
    var urlSaveMessage by remember { mutableStateOf<String?>(null) }
    var isUrlSaveSuccess by remember { mutableStateOf(false) }

    // Change Password sub-section
    var showChangePasswordSection by rememberSaveable { mutableStateOf(false) }
    var newPasswordInput by rememberSaveable { mutableStateOf("") }
    var passwordChangeMessage by remember { mutableStateOf<String?>(null) }

    // Real-time synchronization of Target URL from Firestore across all devices
    LaunchedEffect(Unit) {
        appConfigRepo.observeTargetUrl(TARGET_URL).collect { newUrl ->
            if (newUrl.isNotBlank() && newUrl != currentTargetUrl) {
                currentTargetUrl = newUrl
                val wv = webViewInstance
                if (wv != null && !isPostLoginUrl(wv.url)) {
                    wv.post {
                        wv.loadUrl(newUrl)
                    }
                }
            }
        }
    }

    // Infinite rotation animation for refresh icon when refreshing or loading
    val infiniteTransition = rememberInfiniteTransition(label = "kiosk_refresh_rotation")
    val refreshRotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "kiosk_refresh_angle"
    )

    // Manual screen expansion percentages (Top and Bottom) loaded & saved permanently via SharedPreferences
    var topExpansionPercent by rememberSaveable {
        mutableFloatStateOf(
            sharedPrefs.getFloat(KEY_TOP_EXPANSION, DEFAULT_TOP_EXPANSION)
                .coerceIn(MIN_TOP_EXPANSION, MAX_TOP_EXPANSION)
        )
    }
    var bottomExpansionPercent by rememberSaveable {
        mutableFloatStateOf(
            sharedPrefs.getFloat(KEY_BOTTOM_EXPANSION, DEFAULT_BOTTOM_EXPANSION)
                .coerceIn(MIN_BOTTOM_EXPANSION, MAX_BOTTOM_EXPANSION)
        )
    }
    var showExpansionControls by rememberSaveable { mutableStateOf(false) }
    var showTopActionButtons by rememberSaveable { mutableStateOf(false) }

    // Modern floating download notification event state
    var activeDownloadEvent by remember { mutableStateOf<DownloadSuccessEvent?>(null) }

    DisposableEffect(Unit) {
        onDownloadFinishedListener = { event ->
            activeDownloadEvent = event
        }
        onDispose {
            if (onDownloadFinishedListener != null) {
                onDownloadFinishedListener = null
            }
        }
    }

    // Check if user is on the main Gemini Canvas page or in login flow
    val isCanvasActive = isGeminiCanvasUrl(currentUrl)

    // File upload callback handler for WebChromeClient (file input / camera picker)
    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intentData = result.data
            val results: Array<Uri>? = when {
                intentData?.clipData != null -> {
                    val count = intentData.clipData!!.itemCount
                    Array(count) { i -> intentData.clipData!!.getItemAt(i).uri }
                }
                intentData?.data != null -> {
                    arrayOf(intentData.data!!)
                }
                else -> null
            }
            fileUploadCallback?.onReceiveValue(results)
        } else {
            fileUploadCallback?.onReceiveValue(null)
        }
        fileUploadCallback = null
    }

    // Permission request launcher for WebRTC (Microphone, Camera, Audio recording)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Handled dynamically
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.MODIFY_AUDIO_SETTINGS
            )
        )
    }

    // Pending download task waiting for storage/notification permissions
    var pendingDownloadTask by remember { mutableStateOf<PendingDownloadTask?>(null) }

    // Runtime Permission Launcher specifically for Storage and Notifications
    val downloadPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        val pending = pendingDownloadTask
        pendingDownloadTask = null
        if (pending != null) {
            val wv = webViewInstance
            executeDownloadManagerRequest(
                context = context,
                webView = wv,
                url = pending.url,
                userAgent = pending.userAgent,
                contentDisposition = pending.contentDisposition,
                mimetype = pending.mimetype
            )
        }
    }

    // Lambda to verify runtime permissions before executing download
    val requestDownloadWithPermissionCheck: (String, String, String, String) -> Unit = remember(context, webViewInstance) {
        { url, userAgent, contentDisposition, mimetype ->
            val required = getRequiredDownloadPermissions()
            val missing = required.filter {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }
            if (missing.isNotEmpty()) {
                pendingDownloadTask = PendingDownloadTask(url, userAgent, contentDisposition, mimetype)
                downloadPermissionLauncher.launch(missing.toTypedArray())
            } else {
                executeDownloadManagerRequest(
                    context = context,
                    webView = webViewInstance,
                    url = url,
                    userAgent = userAgent,
                    contentDisposition = contentDisposition,
                    mimetype = mimetype
                )
            }
        }
    }

    // Register receiver to scan completed HTTP/HTTPS downloads into the phone gallery / media store
    DisposableEffect(context) {
        val downloadCompleteReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: return
                if (downloadId != -1L && ctx != null) {
                    val downloadManager = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager?.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (statusIdx != -1 && cursor.getInt(statusIdx) == DownloadManager.STATUS_SUCCESSFUL) {
                            val localUriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            val mimeTypeIdx = cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)
                            val titleIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                            val localUri = if (localUriIdx != -1) cursor.getString(localUriIdx) else null
                            val mimeType = if (mimeTypeIdx != -1) cursor.getString(mimeTypeIdx) else null
                            val title = if (titleIdx != -1) cursor.getString(titleIdx) else null

                            if (localUri != null) {
                                val filePath = Uri.parse(localUri).path
                                if (filePath != null) {
                                    MediaScannerConnection.scanFile(
                                        ctx,
                                        arrayOf(filePath),
                                        arrayOf(mimeType ?: "*/*")
                                    ) { _, _ -> }
                                }
                            }
                            val displayFileName = title ?: localUri?.substringAfterLast("/")?.substringAfterLast("%2F") ?: "Berkas"
                            val resolvedMime = resolveMimeType(displayFileName, mimeType, localUri ?: "")
                            val downloadType = getDownloadType(displayFileName, resolvedMime)
                            val loc = getStorageLocationLabel(downloadType)

                            notifyDownloadCompleted(ctx, displayFileName, resolvedMime, localUri ?: "")
                        }
                        cursor.close()
                    }
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(downloadCompleteReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(downloadCompleteReceiver, filter)
        }

        onDispose {
            try {
                context.unregisterReceiver(downloadCompleteReceiver)
            } catch (_: Exception) { }
            CookieManager.getInstance().flush()
            webViewInstance?.destroy()
            webViewInstance = null
        }
    }

    // Handle back button for webview history navigation
    BackHandler(enabled = webViewInstance?.canGoBack() == true) {
        webViewInstance?.goBack()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ToskaDark)
            .testTag("kiosk_browser_root")
    ) {
        val totalHeight = maxHeight
        val totalWidth = maxWidth

        // Dynamic top & bottom extension based on user manual settings or defaults
        val expandedHeight = totalHeight * (1f + (topExpansionPercent + bottomExpansionPercent) / 100f)
        val topOffsetY = -(totalHeight * (topExpansionPercent / 100f))

        // Left extended 3% and Right extended 3% (total 106% width, centered horizontally)
        val expandedWidth = totalWidth * 1.06f

        // 1. High-Performance Modern Chromium Engine (Dynamically expanded: top & bottom controlled manually, 3% left & right centered)
        AndroidView(
            modifier = Modifier
                .requiredWidth(expandedWidth)
                .requiredHeight(expandedHeight)
                .align(Alignment.TopCenter)
                .absoluteOffset(y = topOffsetY)
                .testTag("kiosk_webview"),
            factory = { ctx ->
                val customWebView = object : WebView(ctx) {
                    override fun overScrollBy(
                        deltaX: Int, deltaY: Int,
                        scrollX: Int, scrollY: Int,
                        scrollRangeX: Int, scrollRangeY: Int,
                        maxOverScrollX: Int, maxOverScrollY: Int,
                        isTouchEvent: Boolean
                    ): Boolean {
                        return false
                    }
                }

                customWebView.apply {
                    val currentWebView = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Background and scrolling configuration
                    setBackgroundColor(android.graphics.Color.WHITE)
                    overScrollMode = View.OVER_SCROLL_NEVER
                    isNestedScrollingEnabled = false
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false

                    // Comprehensive Chromium Browser Settings
                    settings.apply {
                        // Core Javascript & HTML5 Engine
                        javaScriptEnabled = true
                        javaScriptCanOpenWindowsAutomatically = true

                        // Storage & Offline Caching
                        domStorageEnabled = true
                        databaseEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT

                        // Responsive Viewport & Rendering
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        setSupportZoom(false)
                        builtInZoomControls = false
                        displayZoomControls = false

                        // Hardware Acceleration & High-performance Rendering
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        allowFileAccess = true
                        allowContentAccess = true
                        allowFileAccessFromFileURLs = true
                        allowUniversalAccessFromFileURLs = true

                        // Media, Audio, Video & WebRTC Capabilities
                        mediaPlaybackRequiresUserGesture = false
                        setGeolocationEnabled(true)

                        // Multiple Windows & Popup handling
                        setSupportMultipleWindows(true)

                        // Modern Chrome User-Agent without WebView identifiers
                        val defaultUa = userAgentString
                        userAgentString = defaultUa
                            .replace("; wv", "")
                            .replace(Regex("Version/\\d+\\.\\d+\\s*"), "")
                    }

                    // Persistent Cookie Management with Third-Party & Cross-Origin Cookies enabled
                    CookieManager.getInstance().apply {
                        setAcceptCookie(true)
                        setAcceptThirdPartyCookies(currentWebView, true)
                        flush()
                    }

                    // Javascript Interface to capture blob/base64 generated images & voice downloads
                    addJavascriptInterface(
                        BlobDownloadInterface(
                            context = ctx,
                            webViewProvider = { currentWebView },
                            downloadTrigger = requestDownloadWithPermissionCheck
                        ),
                        "AndroidBlobDownloader"
                    )

                    // Download manager listener for file downloads with full blob: and data: support
                    setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
                        requestDownloadWithPermissionCheck(
                            url,
                            userAgent ?: "",
                            contentDisposition ?: "",
                            mimetype ?: ""
                        )
                    }

                    // Full-featured WebChromeClient (WebRTC, Geolocation, Media, Popups, Console)
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            loadingProgress = newProgress
                            // Continuously clean and hide Gemini top buttons as DOM nodes render
                            injectTopGeminiButtonHider(view)
                            // Inject download interceptor continuously during DOM construction
                            injectBlobAndDownloadInterceptor(view)
                            if (newProgress >= 100) {
                                isLoading = false
                                isRefreshing = false
                            }
                        }

                        // Support WebRTC (Microphone, Camera, Audio capture for Gemini Canvas voice/video)
                        override fun onPermissionRequest(request: PermissionRequest?) {
                            request?.grant(request.resources)
                        }

                        // Support Geolocation
                        override fun onGeolocationPermissionsShowPrompt(
                            origin: String?,
                            callback: GeolocationPermissions.Callback?
                        ) {
                            callback?.invoke(origin, true, false)
                        }

                        // File picker (Images, Audio, Documents, Camera capture)
                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallbackParam: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            fileUploadCallback?.onReceiveValue(null)
                            fileUploadCallback = filePathCallbackParam

                            return try {
                                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "*/*"
                                }
                                fileChooserLauncher.launch(intent)
                                true
                            } catch (e: Exception) {
                                fileUploadCallback?.onReceiveValue(null)
                                fileUploadCallback = null
                                false
                            }
                        }

                        // Support window.open / popups (such as affiliate redirects, login prompts)
                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: Message?
                        ): Boolean {
                            val transport = resultMsg?.obj as? WebView.WebViewTransport
                            val tempWebView = WebView(ctx)
                            tempWebView.webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(v: WebView?, req: WebResourceRequest?): Boolean {
                                    val targetUri = req?.url?.toString() ?: return false
                                    val isHandled = handleExternalNavigation(ctx, targetUri, view)
                                    if (!isHandled) {
                                        view?.loadUrl(targetUri)
                                    }
                                    return true
                                }
                            }
                            transport?.webView = tempWebView
                            resultMsg?.sendToTarget()
                            return true
                        }

                        // Support standard web JS alert / confirm / prompt
                        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
                            result?.confirm()
                            return true
                        }

                        override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                            result?.confirm()
                            return true
                        }

                        override fun onJsPrompt(
                            view: WebView?,
                            url: String?,
                            message: String?,
                            defaultValue: String?,
                            result: JsPromptResult?
                        ): Boolean {
                            result?.confirm(defaultValue ?: "")
                            return true
                        }

                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            return true
                        }
                    }

                    // Web navigation, Shopee affiliate app linking, and error handling
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val uri = request?.url ?: return false
                            val uriString = uri.toString()
                            if (isDirectDownloadableUrl(uriString)) {
                                val wv = view ?: currentWebView
                                val ua = wv.settings.userAgentString
                                requestDownloadWithPermissionCheck(uriString, ua, "", "")
                                return true
                            }
                            return handleExternalNavigation(ctx, uriString, view)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            if (url == null) return false
                            if (isDirectDownloadableUrl(url)) {
                                val wv = view ?: currentWebView
                                val ua = wv.settings.userAgentString
                                requestDownloadWithPermissionCheck(url, ua, "", "")
                                return true
                            }
                            return handleExternalNavigation(ctx, url, view)
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            currentUrl = url
                            isLoading = true
                            hasError = false

                            injectTopGeminiButtonHider(view)
                            injectDisclaimerCleaner(view)
                            injectBlobAndDownloadInterceptor(view)

                            if (isGeminiCanvasUrl(url)) {
                                applyStaticScreenLock(view)
                            }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            currentUrl = url
                            isLoading = false
                            isRefreshing = false
                            CookieManager.getInstance().flush()

                            // Check if login completed and landed on Google home/myaccount page
                            if (isPostLoginUrl(url)) {
                                view?.post {
                                    view.loadUrl(currentTargetUrl)
                                }
                                return
                            }

                            // Hide top Gemini button, disclaimer text, and expand top area
                            injectTopGeminiButtonHider(view)
                            injectDisclaimerCleaner(view)

                            if (isGeminiCanvasUrl(url)) {
                                // Lock screen to static (no scrolling) while keeping buttons/actions fully functional
                                applyStaticScreenLock(view)
                            }

                            // Inject robust interception script for blob: and data: generated images & voice/audio files
                            injectBlobAndDownloadInterceptor(view)
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                isLoading = false
                                isRefreshing = false
                                hasError = true
                                errorMessage = error?.description?.toString() ?: "Gagal memuat halaman web."
                            }
                        }

                        @SuppressLint("WebViewClientOnReceivedSslError")
                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?
                        ) {
                            handler?.proceed()
                        }
                    }

                    loadUrl(currentTargetUrl)
                    webViewInstance = this
                }
            },
            update = { webView ->
                webViewInstance = webView
            }
        )

        // 2. Floating Minimalist Controls (Top-Right discreet buttons: Screen expansion, Login, Refresh, and Toggle Hide/Show)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated visibility for the 3 main action buttons
            AnimatedVisibility(
                visible = showTopActionButtons,
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(targetOffsetY = { -it / 2 })
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Button to toggle Manual Screen Expansion Control Panel
                    MalayTopActionButton(
                        icon = Icons.Default.Tune,
                        contentDescription = if (showExpansionControls) "Sembunyikan Pengaturan Perluasan" else "Atur Perluasan Layar HP",
                        testTag = "floating_expansion_button",
                        onClick = {
                            showExpansionControls = !showExpansionControls
                        }
                    )

                    // 2. Google Login Button
                    MalayTopActionButton(
                        icon = Icons.Default.AccountCircle,
                        contentDescription = "Login Akun Google",
                        testTag = "floating_login_button",
                        onClick = {
                            hasError = false
                            isLoading = true
                            webViewInstance?.stopLoading()
                            webViewInstance?.loadUrl(LOGIN_URL)
                        }
                    )

                    // 3. Refresh Page Button
                    MalayTopActionButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Muat Ulang Halaman",
                        testTag = "floating_refresh_button",
                        iconRotation = if (isRefreshing || isLoading) refreshRotationAngle else 0f,
                        onClick = {
                            hasError = false
                            isRefreshing = true
                            val wv = webViewInstance
                            if (wv != null) {
                                wv.stopLoading()
                                val current = wv.url
                                if (!current.isNullOrBlank() && (isGeminiCanvasUrl(current) || current.startsWith("http"))) {
                                    wv.reload()
                                } else {
                                    wv.loadUrl(currentTargetUrl)
                                }
                            }
                        }
                    )
                }
            }

            // 4. Toggle Button to Hide / Show the 3 Top Action Buttons (Transparent when hidden)
            MalayTopActionButton(
                icon = if (showTopActionButtons) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (showTopActionButtons) "Sembunyikan 3 Tombol Atas" else "Tampilkan 3 Tombol Atas",
                testTag = "toggle_top_buttons_button",
                isTransparent = !showTopActionButtons,
                onClick = {
                    showTopActionButtons = !showTopActionButtons
                    if (!showTopActionButtons) {
                        showExpansionControls = false
                    }
                }
            )
        }

        // 3. Manual Screen Expansion Control Panel (Floating & Collapsible / Hideable)
        AnimatedVisibility(
            visible = showExpansionControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 58.dp, start = 16.dp, end = 16.dp)
        ) {
            ScreenExpansionControlPanel(
                topPercent = topExpansionPercent,
                bottomPercent = bottomExpansionPercent,
                onTopChange = {
                    topExpansionPercent = it
                    sharedPrefs.edit().putFloat(KEY_TOP_EXPANSION, it).apply()
                },
                onBottomChange = {
                    bottomExpansionPercent = it
                    sharedPrefs.edit().putFloat(KEY_BOTTOM_EXPANSION, it).apply()
                },
                onReset = {
                    topExpansionPercent = DEFAULT_TOP_EXPANSION
                    bottomExpansionPercent = DEFAULT_BOTTOM_EXPANSION
                    sharedPrefs.edit()
                        .putFloat(KEY_TOP_EXPANSION, DEFAULT_TOP_EXPANSION)
                        .putFloat(KEY_BOTTOM_EXPANSION, DEFAULT_BOTTOM_EXPANSION)
                        .apply()
                },
                onDismiss = {
                    showExpansionControls = false
                }
            )
        }

        // 4. Loading state overlay (only show full center indicator on initial fresh loads/errors)
        AnimatedVisibility(
            visible = isLoading && !isRefreshing && !hasError,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = ToskaDark.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = ToskaTeal,
                        trackColor = ToskaBright.copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isCanvasActive) "Memuat GuberStudio..." else "Memuat Halaman Masuk...",
                        color = MalayGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 5. Error / Disconnected state overlay with retry button
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ToskaDark)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Koneksi Terputus",
                        tint = MalayGold,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Gagal Membuka Halaman",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "Periksa koneksi internet perangkat Anda.",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            hasError = false
                            isLoading = true
                            webViewInstance?.loadUrl(currentTargetUrl)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ToskaBright,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("retry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Coba Lagi"
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Coba Lagi", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 6. Modern Floating Download Notification Banner (Top-Center Malay Card)
        ModernDownloadNotificationBanner(
            event = activeDownloadEvent,
            onDismiss = { activeDownloadEvent = null },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 7. Minimalist Side Admin Tab Button (Hidden/Collapsed edge trigger)
        AnimatedVisibility(
            visible = !isSideMenuOpen,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(ToskaDarkMedium.copy(alpha = 0.85f))
                    .border(
                        BorderStroke(1.dp, MalayGold.copy(alpha = 0.6f)),
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
                    .clickable {
                        adminPasswordError = null
                        urlSaveMessage = null
                        if (isAdminLoggedIn) {
                            newTargetUrlInput = currentTargetUrl
                        }
                        isSideMenuOpen = true
                    }
                    .padding(horizontal = 7.dp, vertical = 14.dp)
                    .testTag("side_admin_tab_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAdminLoggedIn) Icons.Default.AdminPanelSettings else Icons.Default.Lock,
                    contentDescription = "Buka Menu Admin",
                    tint = MalayGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 8. Minimalist Side Admin Drawer (Password-protected, URL changer & device sync)
        AnimatedVisibility(
            visible = isSideMenuOpen,
            enter = fadeIn(tween(200)) + slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(250)),
            exit = fadeOut(tween(150)) + slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(200)),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 330.dp)
                    .padding(vertical = 12.dp, horizontal = 6.dp)
                    .testTag("side_admin_drawer"),
                shape = RoundedCornerShape(18.dp),
                color = ToskaDark.copy(alpha = 0.98f),
                border = BorderStroke(1.5.dp, MalayGold.copy(alpha = 0.75f)),
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header with title and Hide/Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MalayGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Admin Kiosk Link",
                                color = MalayGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = {
                                isSideMenuOpen = false
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("close_side_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Sembunyikan Menu",
                                tint = Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Realtime Firebase Status Badge
                    Surface(
                        color = ToskaDarkMedium,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.8.dp, ToskaBright.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Text(
                                text = "Firebase Real-time Sync Aktif",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isAdminLoggedIn) {
                        // Section 1: Admin Password Entry
                        Text(
                            text = "Masuk Sebagai Admin",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Masukkan password untuk mengubah link tujuan aplikasi ini.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = adminPasswordInput,
                            onValueChange = {
                                adminPasswordInput = it
                                adminPasswordError = null
                            },
                            label = { Text("Password Admin", color = Color.LightGray) },
                            placeholder = { Text("Masukkan password", color = Color.Gray) },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isPasswordVisible) "Sembunyikan password" else "Tampilkan password",
                                        tint = MalayGold
                                    )
                                }
                            },
                            isError = adminPasswordError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MalayGold,
                                unfocusedBorderColor = ToskaBright.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = MalayGold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_password_input")
                        )

                        if (adminPasswordError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = adminPasswordError ?: "",
                                color = Color(0xFFFF5252),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    adminPasswordInput = ""
                                    adminPasswordError = null
                                    isSideMenuOpen = false
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.LightGray
                                ),
                                border = BorderStroke(1.dp, Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Tutup", fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    if (appConfigRepo.verifyAdminPassword(adminPasswordInput.trim())) {
                                        isAdminLoggedIn = true
                                        adminPasswordInput = ""
                                        adminPasswordError = null
                                        newTargetUrlInput = currentTargetUrl
                                    } else {
                                        adminPasswordError = "Password salah! (Bawaan: admin)"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MalayGold,
                                    contentColor = ToskaDark
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_login_submit_button")
                            ) {
                                Text("Masuk", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tips: Password awal sistem adalah \"admin\". Anda dapat mengubahnya setelah masuk.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    } else {
                        // Section 2: Logged-in Admin Screen - Change URL & Sync
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ToskaBright.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MalayGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Admin Terverifikasi",
                                        color = MalayGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    isAdminLoggedIn = false
                                    urlSaveMessage = null
                                }
                            ) {
                                Text("Keluar", color = Color(0xFFFF8A80), fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Current Synced URL Display
                        Text(
                            text = "Link Aktif Saat Ini:",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = ToskaDarkMedium,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.8.dp, ToskaBright.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentTargetUrl,
                                color = MalayGoldLight,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp),
                                maxLines = 3
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // New URL Input
                        Text(
                            text = "Masukkan Link Baru:",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = newTargetUrlInput,
                            onValueChange = {
                                newTargetUrlInput = it
                                urlSaveMessage = null
                            },
                            placeholder = { Text("https://...", color = Color.Gray) },
                            singleLine = false,
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MalayGold,
                                unfocusedBorderColor = ToskaBright.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = MalayGold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_target_url_input")
                        )

                        if (urlSaveMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = urlSaveMessage ?: "",
                                color = if (isUrlSaveSuccess) Color(0xFF69F0AE) else Color(0xFFFF5252),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Save & Sync Button
                        Button(
                            onClick = {
                                val target = newTargetUrlInput.trim()
                                if (target.isBlank() || (!target.startsWith("http://") && !target.startsWith("https://"))) {
                                    urlSaveMessage = "URL harus diawali https:// atau http://"
                                    isUrlSaveSuccess = false
                                    return@Button
                                }
                                coroutineScope.launch {
                                    isSavingUrl = true
                                    urlSaveMessage = null
                                    val result = appConfigRepo.updateTargetUrl(target)
                                    isSavingUrl = false
                                    if (result.isSuccess) {
                                        isUrlSaveSuccess = true
                                        urlSaveMessage = "Sukses! Link telah tersinkron ke semua perangkat."
                                        currentTargetUrl = target
                                        webViewInstance?.loadUrl(target)
                                        Toast.makeText(context, "Link berhasil disinkronkan ke semua perangkat!", Toast.LENGTH_LONG).show()
                                    } else {
                                        isUrlSaveSuccess = false
                                        urlSaveMessage = result.exceptionOrNull()?.message ?: "Gagal memperbarui link"
                                    }
                                }
                            },
                            enabled = !isSavingUrl && newTargetUrlInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ToskaBright,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_save_url_button")
                        ) {
                            if (isSavingUrl) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Menyinkronkan...", fontSize = 13.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simpan & Sinkronkan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Change Password Sub-section
                        TextButton(
                            onClick = {
                                showChangePasswordSection = !showChangePasswordSection
                                passwordChangeMessage = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (showChangePasswordSection) "Tutup Ganti Password" else "Ubah Password Admin",
                                color = MalayGold,
                                fontSize = 12.sp
                            )
                        }

                        if (showChangePasswordSection) {
                            OutlinedTextField(
                                value = newPasswordInput,
                                onValueChange = {
                                    newPasswordInput = it
                                    passwordChangeMessage = null
                                },
                                label = { Text("Password Baru", color = Color.LightGray, fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MalayGold,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_new_password_input")
                            )

                            if (passwordChangeMessage != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = passwordChangeMessage ?: "",
                                    color = Color(0xFF69F0AE),
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (newPasswordInput.trim().length >= 4) {
                                        appConfigRepo.updateAdminPassword(newPasswordInput.trim())
                                        passwordChangeMessage = "Password admin berhasil diubah!"
                                        newPasswordInput = ""
                                    } else {
                                        passwordChangeMessage = "Minimal 4 karakter!"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ToskaDarkMedium,
                                    contentColor = MalayGold
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Simpan Password Baru", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Hide Menu button
                        OutlinedButton(
                            onClick = {
                                isSideMenuOpen = false
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.LightGray
                            ),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sembunyikan Menu", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Elegant collapsible control panel to manually adjust top and bottom screen expansion on phone.
 */
@Composable
fun ScreenExpansionControlPanel(
    topPercent: Float,
    bottomPercent: Float,
    onTopChange: (Float) -> Unit,
    onBottomChange: (Float) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .widthIn(max = 380.dp)
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {}
            .testTag("expansion_control_panel"),
        shape = RoundedCornerShape(20.dp),
        color = ToskaDark.copy(alpha = 0.96f),
        border = BorderStroke(1.5.dp, MalayGold.copy(alpha = 0.85f)),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MalayGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Atur Perluasan Layar HP",
                        color = MalayGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("close_expansion_panel_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Sembunyikan Panel",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Top Expansion Control (Perluasan Atas)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Perluasan Atas HP (Min 1%)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = ToskaBright.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, MalayGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${topPercent.toInt()}%",
                            color = MalayGoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onTopChange((topPercent - 1f).coerceAtLeast(MIN_TOP_EXPANSION)) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ToskaDarkMedium),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MalayGold)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Kurang Atas", modifier = Modifier.size(16.dp))
                    }

                    Slider(
                        value = topPercent.coerceIn(MIN_TOP_EXPANSION, MAX_TOP_EXPANSION),
                        onValueChange = { onTopChange(it.coerceIn(MIN_TOP_EXPANSION, MAX_TOP_EXPANSION)) },
                        valueRange = MIN_TOP_EXPANSION..MAX_TOP_EXPANSION,
                        steps = 23,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("top_expansion_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MalayGold,
                            activeTrackColor = ToskaTeal,
                            inactiveTrackColor = ToskaDarkMedium
                        )
                    )

                    IconButton(
                        onClick = { onTopChange((topPercent + 1f).coerceAtMost(MAX_TOP_EXPANSION)) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ToskaDarkMedium),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MalayGold)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Atas", modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Bottom Expansion Control (Perluasan Bawah)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Perluasan Bawah HP (Min 8%)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = ToskaBright.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, MalayGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${bottomPercent.toInt()}%",
                            color = MalayGoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onBottomChange((bottomPercent - 1f).coerceAtLeast(MIN_BOTTOM_EXPANSION)) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ToskaDarkMedium),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MalayGold)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Kurang Bawah", modifier = Modifier.size(16.dp))
                    }

                    Slider(
                        value = bottomPercent.coerceIn(MIN_BOTTOM_EXPANSION, MAX_BOTTOM_EXPANSION),
                        onValueChange = { onBottomChange(it.coerceIn(MIN_BOTTOM_EXPANSION, MAX_BOTTOM_EXPANSION)) },
                        valueRange = MIN_BOTTOM_EXPANSION..MAX_BOTTOM_EXPANSION,
                        steps = 31,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bottom_expansion_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MalayGold,
                            activeTrackColor = ToskaTeal,
                            inactiveTrackColor = ToskaDarkMedium
                        )
                    )

                    IconButton(
                        onClick = { onBottomChange((bottomPercent + 1f).coerceAtMost(MAX_BOTTOM_EXPANSION)) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ToskaDarkMedium),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MalayGold)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Bawah", modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Quick Preset / Reset to Default Button
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_expansion_default_button"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MalayGold.copy(alpha = 0.75f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MalayGoldLight)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset ke Default (Atas 4% / Bawah 21%)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            // Hide/Close Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hide_expansion_panel_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ToskaBright,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sembunyikan Menu", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

/**
 * Injects a clean CSS rule that visually hides only the top button / link
 * leading to https://gemini.google.com/app, without interfering with page execution or layout.
 */
private fun injectTopGeminiButtonHider(webView: WebView?) {
    val hiderJs = """
        (function() {
            try {
                if (!document.getElementById('__guber_hide_top_gemini_css')) {
                    var style = document.createElement('style');
                    style.id = '__guber_hide_top_gemini_css';
                    style.innerHTML = 
                        'a[href*="gemini.google.com/app" i], ' +
                        'a[href*="bard.google.com" i], ' +
                        'header a[href*="gemini.google.com" i]:not([href*="share.gemini.google" i]), ' +
                        'nav a[href*="gemini.google.com" i]:not([href*="share.gemini.google" i]), ' +
                        'header [data-test-id*="gemini-logo" i], ' +
                        'nav [data-test-id*="gemini-logo" i], ' +
                        '.gemini-logo { ' +
                        '  display: none !important; ' +
                        '  visibility: hidden !important; ' +
                        '  opacity: 0 !important; ' +
                        '  pointer-events: none !important; ' +
                        '}';
                    (document.head || document.documentElement).appendChild(style);
                }
            } catch(e) {}
        })();
    """.trimIndent()
    webView?.evaluateJavascript(hiderJs, null)
}

/**
 * Handles external URL navigation, including Shopee affiliate links, custom app schemes,
 * and external browser routing.
 */
private fun handleExternalNavigation(context: Context, url: String, webView: WebView?): Boolean {
    try {
        val uri = Uri.parse(url)
        val scheme = uri.scheme?.lowercase() ?: ""

        // Handle Android Intent URIs (common for Shopee, Tokopedia, Lazada, Play Store apps)
        if (scheme == "intent") {
            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            if (intent != null) {
                val packageManager = context.packageManager
                val info = intent.resolveActivity(packageManager)
                if (info != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                }
                // Fallback if app is not installed
                val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                if (!fallbackUrl.isNullOrEmpty()) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(browserIntent)
                    return true
                }
                val packageName = intent.`package`
                if (!packageName.isNullOrEmpty()) {
                    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (marketIntent.resolveActivity(packageManager) != null) {
                        context.startActivity(marketIntent)
                        return true
                    }
                }
            }
            return true
        }

        // Handle custom app schemes (shopee://, shopeeid://, whatsapp://, market://, tel:, mailto:, etc.)
        if (scheme != "http" && scheme != "https" && scheme != "blob" && scheme != "data" && scheme != "javascript") {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return true
            } else if (scheme.contains("shopee")) {
                // If Shopee app is not installed, open in Shopee website / Play Store
                val webFallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shopee.co.id")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webFallbackIntent)
                return true
            }
            return true
        }

        // Handle external affiliate links and third-party stores (Shopee, Tokopedia, TikTok, Lazada, etc.)
        val host = uri.host?.lowercase() ?: ""
        val isShopee = host.contains("shopee") || host.contains("shp.ee")
        if (isShopee) {
            pendingShopeeUrl = url
            return true
        }
        val isExternalAffiliateOrStore = host.contains("tokopedia") || host.contains("lazada") ||
                host.contains("tiktok.com") || host.contains("whatsapp.com") ||
                host.contains("play.google.com")

        // Allow Google authentication, accounts, Google domains, and Gemini navigation to stay strictly inside WebView
        val isGoogleAuthOrGemini = host.contains("google.com") ||
                host.contains("google.") ||
                host.contains("gemini.google") ||
                host.contains("share.gemini.google") ||
                host.contains("googleusercontent.com") ||
                host.contains("gstatic.com") ||
                host.contains("googleapis.com")

        if (isGoogleAuthOrGemini) {
            // Keep all Google accounts, authentication, and Gemini navigation inside WebView
            return false
        }

        if (isExternalAffiliateOrStore) {
            val externalIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(externalIntent)
                return true
            } catch (e: Exception) {
                return false
            }
        }

        return false
    } catch (e: Exception) {
        return false
    }
}

/**
 * Injects a script that hides "Report unsafe content", "Try Gemini Canvas", terms, privacy,
 * report abuse, and all link/action buttons at the bottom of the page, making the area clean and unclickable.
 */
private fun injectDisclaimerCleaner(webView: WebView?) {
    val disclaimerJs = """
        (function() {
            function purgeDisclaimer() {
                var phrasesToHide = [
                    'report unsafe content',
                    'laporkan konten tidak aman',
                    'report unsafe',
                    'report abuse',
                    'laporkan penyalahgunaan',
                    'try gemini canvas',
                    'coba gemini canvas',
                    'try gemini',
                    'coba gemini',
                    'persyaratan layanan',
                    'terms of service',
                    'kebijakan privasi',
                    'privacy policy',
                    'bantuan',
                    'help',
                    'kirim masukan',
                    'send feedback'
                ];

                try {
                    var allInteractives = document.querySelectorAll('a, button, [role="button"], span, p, label, footer');
                    allInteractives.forEach(function(el) {
                        var txt = (el.innerText || el.textContent || '').trim().toLowerCase();
                        var href = (el.getAttribute('href') || '').toLowerCase();
                        var aria = (el.getAttribute('aria-label') || '').toLowerCase();

                        // Never touch main action buttons like Continue / Lanjutkan / Buka / Submit
                        if (txt === 'lanjutkan' || txt === 'continue' || txt.startsWith('lanjutkan') || txt.startsWith('continue')) {
                            el.style.setProperty('visibility', 'visible', 'important');
                            el.style.setProperty('opacity', '1', 'important');
                            el.style.setProperty('pointer-events', 'auto', 'important');
                            return;
                        }

                        // Check if bottom footer/disclaimer link or matching phrases
                        var isReportOrBottomLink = href.includes('report') || href.includes('abuse') || 
                                                   href.includes('support.google.com') || href.includes('policies.google.com') ||
                                                   href.includes('google.com/intl') || href.includes('gemini.google.com');

                        if (isReportOrBottomLink && txt !== 'lanjutkan' && txt !== 'continue') {
                            el.style.setProperty('visibility', 'hidden', 'important');
                            el.style.setProperty('opacity', '0', 'important');
                            el.style.setProperty('pointer-events', 'none', 'important');
                            el.style.setProperty('cursor', 'default', 'important');
                            return;
                        }

                        for (var i = 0; i < phrasesToHide.length; i++) {
                            var phrase = phrasesToHide[i];
                            if (txt === phrase || (txt.includes(phrase) && txt.length < 50) || aria.includes(phrase)) {
                                el.style.setProperty('visibility', 'hidden', 'important');
                                el.style.setProperty('opacity', '0', 'important');
                                el.style.setProperty('pointer-events', 'none', 'important');
                                el.style.setProperty('cursor', 'default', 'important');
                                break;
                            }
                        }
                    });
                } catch(e) {}

                // Inject CSS rules for instant zero-flicker suppression of all bottom disclaimer, fixed footer, & report elements
                if (!document.getElementById('__guber_disclaimer_css')) {
                    var style = document.createElement('style');
                    style.id = '__guber_disclaimer_css';
                    style.innerHTML = 
                        'footer, ' +
                        '[role="contentinfo"], ' +
                        '.app-footer, ' +
                        '.bottom-bar, ' +
                        '.disclaimer-footer, ' +
                        '.footer-container, ' +
                        'a[href*="report" i], ' +
                        'a[href*="abuse" i], ' +
                        'a[href*="support.google.com" i], ' +
                        'a[href*="policies.google.com" i], ' +
                        'a[href*="google.com/intl" i], ' +
                        '[aria-label*="tidak aman" i], ' +
                        '[aria-label*="report" i], ' +
                        '[aria-label*="abuse" i], ' +
                        '[aria-label*="unsafe" i] { ' +
                        '  display: none !important; ' +
                        '  visibility: hidden !important; ' +
                        '  opacity: 0 !important; ' +
                        '  pointer-events: none !important; ' +
                        '  height: 0px !important; ' +
                        '  min-height: 0px !important; ' +
                        '  max-height: 0px !important; ' +
                        '  margin: 0px !important; ' +
                        '  padding: 0px !important; ' +
                        '  cursor: default !important; ' +
                        '}';
                    (document.head || document.documentElement).appendChild(style);
                }
            }

            purgeDisclaimer();

            // Intercept clicks on any report or bottom abuse links so they can never be clicked
            if (!window.__guber_disclaimer_click_interceptor) {
                window.__guber_disclaimer_click_interceptor = true;
                document.addEventListener('click', function(e) {
                    var target = e.target;
                    if (!target) return;
                    var el = target.closest('a, button, [role="button"]');
                    if (el) {
                        var txt = (el.innerText || el.textContent || '').trim().toLowerCase();
                        var href = (el.getAttribute('href') || '').toLowerCase();
                        var aria = (el.getAttribute('aria-label') || '').toLowerCase();

                        if (txt === 'lanjutkan' || txt === 'continue') return;

                        if (href.includes('report') || href.includes('abuse') || href.includes('policies.google.com') ||
                            href.includes('support.google.com') || txt.includes('report') || txt.includes('laporkan') ||
                            txt.includes('tidak aman') || aria.includes('report') || aria.includes('laporkan')) {
                            e.preventDefault();
                            e.stopPropagation();
                            e.stopImmediatePropagation();
                            return false;
                        }
                    }
                }, true);
            }

            if (!window.__guber_disclaimer_observer_installed) {
                window.__guber_disclaimer_observer_installed = true;
                var observer = new MutationObserver(function() {
                    purgeDisclaimer();
                });
                if (document.body) {
                    observer.observe(document.body, { childList: true, subtree: true });
                } else {
                    document.addEventListener('DOMContentLoaded', function() {
                        if (document.body) observer.observe(document.body, { childList: true, subtree: true });
                    });
                }
            }
        })();
    """.trimIndent()
    webView?.evaluateJavascript(disclaimerJs, null)
}

/**
 * Locks overscroll bouncing while keeping all click,
 * touch, button, and generator controls working smoothly.
 */
private fun applyStaticScreenLock(webView: WebView?) {
    val lockJs = """
        (function() {
            var style = document.getElementById('__guber_static_viewport_lock');
            if (!style) {
                style = document.createElement('style');
                style.id = '__guber_static_viewport_lock';
                style.innerHTML = 
                    'html, body { ' +
                    '  overscroll-behavior: none !important; ' +
                    '  -webkit-overscroll-behavior: none !important; ' +
                    '}';
                (document.head || document.documentElement).appendChild(style);
            }
        })();
    """.trimIndent()
    webView?.evaluateJavascript(lockJs, null)
}

/**
 * Detects binary file type and proper extension directly from decoded magic bytes.
 * This guarantees 100% correct file type identification for WAV, MP3, OGG, FLAC, M4A, PNG, JPEG, WEBP, GIF, PDF.
 */
private fun detectFileTypeFromBytes(bytes: ByteArray): Pair<String, String>? {
    if (bytes.size < 4) return null
    // WAV: "RIFF" .... "WAVE"
    if (bytes.size >= 12 &&
        bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte() && bytes[3] == 0x46.toByte() &&
        bytes[8] == 0x57.toByte() && bytes[9] == 0x41.toByte() && bytes[10] == 0x56.toByte() && bytes[11] == 0x45.toByte()
    ) {
        return Pair("audio/wav", ".wav")
    }
    // MP3 with ID3: "ID3"
    if (bytes[0] == 0x49.toByte() && bytes[1] == 0x44.toByte() && bytes[2] == 0x33.toByte()) {
        return Pair("audio/mpeg", ".mp3")
    }
    // MP3 sync frame: 0xFF, 0xFB or 0xFF, 0xF3 or 0xFF, 0xF2
    if (bytes[0] == 0xFF.toByte() && (bytes[1].toInt() and 0xE0) == 0xE0) {
        return Pair("audio/mpeg", ".mp3")
    }
    // OGG: "OggS"
    if (bytes[0] == 0x4F.toByte() && bytes[1] == 0x67.toByte() && bytes[2] == 0x67.toByte() && bytes[3] == 0x53.toByte()) {
        return Pair("audio/ogg", ".ogg")
    }
    // FLAC: "fLaC"
    if (bytes[0] == 0x66.toByte() && bytes[1] == 0x4C.toByte() && bytes[2] == 0x61.toByte() && bytes[3] == 0x43.toByte()) {
        return Pair("audio/flac", ".flac")
    }
    // M4A/MP4: bytes 4..7 == "ftyp"
    if (bytes.size >= 8 &&
        bytes[4] == 0x66.toByte() && bytes[5] == 0x74.toByte() && bytes[6] == 0x79.toByte() && bytes[7] == 0x70.toByte()
    ) {
        return Pair("audio/mp4", ".m4a")
    }
    // PNG: 0x89, 'P', 'N', 'G'
    if (bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()) {
        return Pair("image/png", ".png")
    }
    // JPEG: 0xFF, 0xD8, 0xFF
    if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
        return Pair("image/jpeg", ".jpg")
    }
    // WEBP: "RIFF" .... "WEBP"
    if (bytes.size >= 12 &&
        bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte() && bytes[3] == 0x46.toByte() &&
        bytes[8] == 0x57.toByte() && bytes[9] == 0x45.toByte() && bytes[10] == 0x42.toByte() && bytes[11] == 0x50.toByte()
    ) {
        return Pair("image/webp", ".webp")
    }
    // GIF: "GIF8"
    if (bytes[0] == 0x47.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte() && bytes[3] == 0x38.toByte()) {
        return Pair("image/gif", ".gif")
    }
    // PDF: "%PDF"
    if (bytes[0] == 0x25.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x44.toByte() && bytes[3] == 0x46.toByte()) {
        return Pair("application/pdf", ".pdf")
    }
    // ZIP / APK: "PK\x03\x04"
    if (bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() && bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()) {
        return Pair("application/zip", ".zip")
    }
    // RAR: "Rar!\x1A\x07"
    if (bytes[0] == 0x52.toByte() && bytes[1] == 0x61.toByte() && bytes[2] == 0x72.toByte() && bytes[3] == 0x21.toByte()) {
        return Pair("application/x-rar-compressed", ".rar")
    }
    // 7-Zip: '7', 'z', 0xBC, 0xAF, 0x27, 0x1C
    if (bytes.size >= 6 &&
        bytes[0] == 0x37.toByte() && bytes[1] == 0x7A.toByte() &&
        bytes[2] == 0xBC.toByte() && bytes[3] == 0xAF.toByte() &&
        bytes[4] == 0x27.toByte() && bytes[5] == 0x1C.toByte()
    ) {
        return Pair("application/x-7z-compressed", ".7z")
    }
    return null
}

/**
 * Checks whether a URL is a direct downloadable media/audio/document/archive/apk file.
 */
private fun isDirectDownloadableUrl(url: String): Boolean {
    val clean = url.lowercase().substringBefore("?").substringBefore("#")
    val extension = if (clean.contains(".")) "." + clean.substringAfterLast(".") else ""
    return when (extension) {
        // Audio
        ".wav", ".mp3", ".ogg", ".m4a", ".flac", ".aac", ".opus", ".weba", ".wma", ".mid" -> true
        // Images
        ".png", ".jpg", ".jpeg", ".webp", ".gif", ".svg", ".bmp", ".ico" -> true
        // Videos
        ".mp4", ".mkv", ".webm", ".avi", ".mov", ".3gp", ".flv", ".wmv" -> true
        // Documents
        ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".csv", ".epub", ".json", ".xml" -> true
        // Archives & Packages
        ".zip", ".rar", ".7z", ".tar", ".gz", ".apk", ".apks", ".xapk" -> true
        else -> false
    }
}

/**
 * Parse filename dynamically from Content-Disposition header (RFC 6266 & RFC 5987).
 */
fun parseFileNameFromContentDisposition(contentDisposition: String?): String? {
    if (contentDisposition.isNullOrBlank()) return null
    try {
        // 1. Try filename*=UTF-8''... (RFC 5987)
        val utf8Match = Regex("""filename\*\s*=\s*(?:UTF-8''|utf-8'')([^;]+)""", RegexOption.IGNORE_CASE).find(contentDisposition)
        if (utf8Match != null) {
            val encoded = utf8Match.groupValues[1].trim().trim('"', '\'')
            val decoded = URLDecoder.decode(encoded, "UTF-8")
            if (decoded.isNotBlank()) return sanitizeFileName(decoded)
        }

        // 2. Try standard filename="..." or filename=...
        val stdMatch = Regex("""filename\s*=\s*(?:"([^"]+)"|'([^']+)'|([^;\s]+))""", RegexOption.IGNORE_CASE).find(contentDisposition)
        if (stdMatch != null) {
            val matched = stdMatch.groupValues[1].ifEmpty {
                stdMatch.groupValues[2].ifEmpty {
                    stdMatch.groupValues[3]
                }
            }.trim().trim('"', '\'')
            if (matched.isNotBlank()) {
                val clean = if (matched.contains("%")) {
                    try { URLDecoder.decode(matched, "UTF-8") } catch (_: Exception) { matched }
                } else matched
                return sanitizeFileName(clean)
            }
        }
    } catch (_: Exception) { }
    return null
}

/**
 * Extract filename dynamically from URL path with decoding and query stripping.
 */
fun parseFileNameFromUrl(url: String?): String? {
    if (url.isNullOrBlank() || url.startsWith("blob:") || url.startsWith("data:")) return null
    return try {
        val uri = Uri.parse(url)
        val lastSegment = uri.lastPathSegment ?: url.substringBefore("?").substringBefore("#").substringAfterLast("/")
        if (lastSegment.isNotBlank()) {
            val decoded = try { URLDecoder.decode(lastSegment, "UTF-8") } catch (_: Exception) { lastSegment }
            val sanitized = sanitizeFileName(decoded)
            if (sanitized.isNotBlank() && sanitized != "/") sanitized else null
        } else null
    } catch (_: Exception) {
        null
    }
}

/**
 * Sanitizes a filename by removing illegal filesystem characters.
 */
fun sanitizeFileName(name: String): String {
    return name.replace(Regex("""[\\/:*?"<>|\r\n\t]"""), "_").trim()
}

/**
 * Resolves a dynamic MIME type based on filename, existing MIME parameter, and URL.
 */
fun resolveMimeType(fileName: String, rawMime: String?, url: String): String {
    val cleanMime = rawMime?.trim()?.lowercase() ?: ""
    if (cleanMime.isNotEmpty() && cleanMime != "application/octet-stream" && cleanMime != "*/*" && cleanMime != "binary/octet-stream") {
        return cleanMime
    }

    val extFromName = if (fileName.contains(".")) fileName.substringAfterLast(".").lowercase() else ""
    if (extFromName.isNotEmpty()) {
        val mapped = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extFromName)
        if (!mapped.isNullOrEmpty()) return mapped
        val custom = getMimeFromExtension(extFromName)
        if (custom.isNotEmpty()) return custom
    }

    val cleanUrl = url.substringBefore("?").substringBefore("#").lowercase()
    val extFromUrl = if (cleanUrl.contains(".")) cleanUrl.substringAfterLast(".") else ""
    if (extFromUrl.isNotEmpty()) {
        val mapped = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extFromUrl)
        if (!mapped.isNullOrEmpty()) return mapped
        val custom = getMimeFromExtension(extFromUrl)
        if (custom.isNotEmpty()) return custom
    }

    return if (cleanMime.isNotEmpty()) cleanMime else "application/octet-stream"
}

/**
 * Fallback extension to MIME mapper for formats Android's MimeTypeMap might miss.
 */
fun getMimeFromExtension(extension: String): String {
    return when (extension.lowercase().removePrefix(".")) {
        "wav" -> "audio/wav"
        "mp3" -> "audio/mpeg"
        "ogg", "oga" -> "audio/ogg"
        "m4a" -> "audio/mp4"
        "flac" -> "audio/flac"
        "aac" -> "audio/aac"
        "opus" -> "audio/opus"
        "weba" -> "audio/webm"
        "mp4" -> "video/mp4"
        "mkv" -> "video/x-matroska"
        "webm" -> "video/webm"
        "avi" -> "video/x-msvideo"
        "mov" -> "video/quicktime"
        "3gp" -> "video/3gpp"
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "ppt" -> "application/vnd.ms-powerpoint"
        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        "txt" -> "text/plain"
        "csv" -> "text/csv"
        "json" -> "application/json"
        "epub" -> "application/epub+zip"
        "zip" -> "application/zip"
        "rar" -> "application/x-rar-compressed"
        "7z" -> "application/x-7z-compressed"
        "tar" -> "application/x-tar"
        "gz" -> "application/gzip"
        "apk" -> "application/vnd.android.package-archive"
        "apks", "xapk" -> "application/vnd.android.package-archive"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "svg" -> "image/svg+xml"
        "bmp" -> "image/bmp"
        "ico" -> "image/x-icon"
        else -> ""
    }
}

/**
 * Resolves both dynamic filename and accurate MIME type for any requested URL.
 */
fun resolveFileNameAndMime(
    url: String,
    contentDisposition: String?,
    rawMime: String?
): Pair<String, String> {
    var fileName = parseFileNameFromContentDisposition(contentDisposition)
    if (fileName.isNullOrBlank()) {
        fileName = parseFileNameFromUrl(url)
    }
    if (fileName.isNullOrBlank()) {
        fileName = URLUtil.guessFileName(url, contentDisposition, rawMime)
    }

    var mime = resolveMimeType(fileName, rawMime, url)

    // Ensure the filename has a valid extension matching the detected MIME type
    val hasExtension = fileName.contains(".") && !fileName.endsWith(".") && !fileName.endsWith(".bin")
    if (!hasExtension) {
        val extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
        val properExt = when {
            !extFromMime.isNullOrEmpty() -> ".$extFromMime"
            mime.contains("wav", true) -> ".wav"
            mime.contains("mpeg", true) || mime.contains("mp3", true) -> ".mp3"
            mime.contains("ogg", true) -> ".ogg"
            mime.contains("flac", true) -> ".flac"
            mime.contains("mp4", true) && mime.startsWith("video/") -> ".mp4"
            mime.contains("mp4", true) && mime.startsWith("audio/") -> ".m4a"
            mime.contains("png", true) -> ".png"
            mime.contains("jpeg", true) || mime.contains("jpg", true) -> ".jpg"
            mime.contains("webp", true) -> ".webp"
            mime.contains("gif", true) -> ".gif"
            mime.contains("pdf", true) -> ".pdf"
            mime.contains("android.package-archive", true) -> ".apk"
            mime.contains("zip", true) -> ".zip"
            else -> ""
        }
        if (properExt.isNotEmpty()) {
            val baseName = if (fileName.endsWith(".bin")) fileName.removeSuffix(".bin") else fileName
            fileName = "$baseName$properExt"
            mime = resolveMimeType(fileName, mime, url)
        }
    }

    return Pair(sanitizeFileName(fileName), mime)
}

/**
 * Returns the suitable public storage destination directory for a given DownloadType.
 */
fun getDestinationDirectory(type: DownloadType): String {
    return when (type) {
        DownloadType.AUDIO_WAV, DownloadType.AUDIO_GENERAL -> Environment.DIRECTORY_MUSIC
        DownloadType.IMAGE -> Environment.DIRECTORY_PICTURES
        DownloadType.VIDEO -> Environment.DIRECTORY_MOVIES
        DownloadType.DOCUMENT -> Environment.DIRECTORY_DOCUMENTS
        DownloadType.ARCHIVE, DownloadType.APK, DownloadType.GENERAL -> Environment.DIRECTORY_DOWNLOADS
    }
}

/**
 * Identifies the exact DownloadType enum from filename and MIME type.
 */
fun getDownloadType(fileName: String, mime: String): DownloadType {
    val lowerName = fileName.lowercase()
    val lowerMime = mime.lowercase()

    return when {
        lowerMime.contains("wav") || lowerName.endsWith(".wav") -> DownloadType.AUDIO_WAV
        isAudioContent(lowerMime, lowerName) -> DownloadType.AUDIO_GENERAL
        isImageContent(lowerMime, lowerName) -> DownloadType.IMAGE
        isVideoContent(lowerMime, lowerName) -> DownloadType.VIDEO
        isApkContent(lowerMime, lowerName) -> DownloadType.APK
        isArchiveContent(lowerMime, lowerName) -> DownloadType.ARCHIVE
        isDocumentContent(lowerMime, lowerName) -> DownloadType.DOCUMENT
        else -> DownloadType.GENERAL
    }
}

/**
 * Human-readable storage location text in Indonesian.
 */
fun getStorageLocationLabel(type: DownloadType): String {
    return when (type) {
        DownloadType.AUDIO_WAV -> "Musik / Audio WAV HP"
        DownloadType.AUDIO_GENERAL -> "Musik & Rekaman HP"
        DownloadType.IMAGE -> "Galeri Foto HP"
        DownloadType.VIDEO -> "Galeri Video & Film HP"
        DownloadType.DOCUMENT -> "Dokumen & Unduhan HP"
        DownloadType.ARCHIVE -> "Folder Unduhan / ZIP HP"
        DownloadType.APK -> "Folder Unduhan / Berkas APK"
        DownloadType.GENERAL -> "Folder Unduhan HP"
    }
}

fun isImageContent(mime: String, name: String): Boolean {
    val m = mime.lowercase()
    val n = name.lowercase()
    return m.startsWith("image/") ||
            n.endsWith(".png") || n.endsWith(".jpg") ||
            n.endsWith(".jpeg") || n.endsWith(".webp") ||
            n.endsWith(".gif") || n.endsWith(".bmp") ||
            n.endsWith(".svg") || n.endsWith(".ico")
}

fun isAudioContent(mime: String, name: String): Boolean {
    val m = mime.lowercase()
    val n = name.lowercase()
    return m.startsWith("audio/") ||
            m.contains("wav") || m.contains("wave") ||
            m.contains("mpeg") || m.contains("mp3") ||
            m.contains("ogg") || m.contains("flac") ||
            m.contains("aac") || m.contains("opus") ||
            n.endsWith(".wav") || n.endsWith(".mp3") ||
            n.endsWith(".ogg") || n.endsWith(".m4a") ||
            n.endsWith(".aac") || n.endsWith(".flac") ||
            n.endsWith(".weba") || n.endsWith(".opus") ||
            n.endsWith(".wma") || n.endsWith(".mid")
}

fun isVideoContent(mime: String, name: String): Boolean {
    val m = mime.lowercase()
    val n = name.lowercase()
    return m.startsWith("video/") ||
            n.endsWith(".mp4") || n.endsWith(".mkv") ||
            n.endsWith(".webm") || n.endsWith(".avi") ||
            n.endsWith(".mov") || n.endsWith(".3gp") ||
            n.endsWith(".flv") || n.endsWith(".wmv")
}

fun isDocumentContent(mime: String, name: String): Boolean {
    val m = mime.lowercase()
    val n = name.lowercase()
    return m.contains("pdf") || m.contains("document") || m.contains("word") ||
            m.contains("sheet") || m.contains("excel") || m.contains("presentation") ||
            m.contains("powerpoint") || m.contains("text/plain") || m.contains("text/csv") ||
            m.contains("epub") || m.contains("json") ||
            n.endsWith(".pdf") || n.endsWith(".doc") || n.endsWith(".docx") ||
            n.endsWith(".xls") || n.endsWith(".xlsx") || n.endsWith(".ppt") ||
            n.endsWith(".pptx") || n.endsWith(".txt") || n.endsWith(".csv") ||
            n.endsWith(".epub") || n.endsWith(".json") || n.endsWith(".xml")
}

fun isArchiveContent(mime: String, name: String): Boolean {
    val m = mime.lowercase()
    val n = name.lowercase()
    return m.contains("zip") || m.contains("compressed") || m.contains("tar") ||
            m.contains("gzip") || m.contains("7z") || m.contains("rar") ||
            n.endsWith(".zip") || n.endsWith(".rar") || n.endsWith(".7z") ||
            n.endsWith(".tar") || n.endsWith(".gz")
}

fun isApkContent(mime: String, name: String): Boolean {
    val m = mime.lowercase()
    val n = name.lowercase()
    return m.contains("android.package-archive") ||
            n.endsWith(".apk") || n.endsWith(".apks") || n.endsWith(".xapk")
}

/**
 * Injects a script into WebView that intercepts dynamic blob: and data: downloads
 * generated by Canvas, AI image generators, and voice generators (Text-to-Speech, WAV, MP3).
 */
private fun injectBlobAndDownloadInterceptor(webView: WebView?) {
    val js = """
        (function() {
            if (window.__guber_blob_interceptor_installed) return;
            window.__guber_blob_interceptor_installed = true;

            // In-memory Blob registry so Blob data is never lost even if revokeObjectURL is called immediately
            window.__guber_blob_map = window.__guber_blob_map || new Map();

            // Hook URL.createObjectURL to store Blob references
            if (window.URL && window.URL.createObjectURL) {
                var origCreateObjectURL = window.URL.createObjectURL;
                window.URL.createObjectURL = function(blob) {
                    var url = origCreateObjectURL.apply(this, arguments);
                    if (blob && (blob instanceof Blob || blob instanceof File)) {
                        window.__guber_blob_map.set(url, blob);
                    }
                    return url;
                };
            }

            // Hook URL.revokeObjectURL to delay revocation until after saving
            if (window.URL && window.URL.revokeObjectURL) {
                var origRevokeObjectURL = window.URL.revokeObjectURL;
                window.URL.revokeObjectURL = function(url) {
                    setTimeout(function() {
                        if (window.__guber_blob_map) {
                            window.__guber_blob_map.delete(url);
                        }
                        try { origRevokeObjectURL.call(window.URL, url); } catch(e) {}
                    }, 120000);
                };
            }

            // Chunked streaming transfer helper to prevent Android WebView JNI/IPC buffer limits on large WAV/MP3 files
            function sendBase64ToNative(base64Data, name, mime) {
                if (!window.AndroidBlobDownloader || !base64Data) return;
                var safeMime = mime || 'audio/wav';
                var safeName = name || ('GuberStudio_Audio_' + Date.now() + '.wav');

                // If small payload (< 128 KB), send directly
                if (base64Data.length < 131072 && window.AndroidBlobDownloader.processBase64) {
                    try {
                        window.AndroidBlobDownloader.processBase64(base64Data, safeName, safeMime);
                        return;
                    } catch (e) {
                        console.warn('Direct transfer failed, fallback to chunked:', e);
                    }
                }

                // Chunked transfer for larger files (WAV audio, MP3, lossless recordings)
                try {
                    var CHUNK_SIZE = 131072; // 128 KB per chunk for maximum IPC stability
                    var totalLen = base64Data.length;
                    var totalChunks = Math.ceil(totalLen / CHUNK_SIZE);
                    var transferId = 'dl_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);

                    if (window.AndroidBlobDownloader.startChunkedDownload) {
                        window.AndroidBlobDownloader.startChunkedDownload(transferId, safeName, safeMime, totalChunks, totalLen);
                        for (var i = 0; i < totalChunks; i++) {
                            var start = i * CHUNK_SIZE;
                            var end = Math.min(start + CHUNK_SIZE, totalLen);
                            var chunk = base64Data.substring(start, end);
                            window.AndroidBlobDownloader.appendDownloadChunk(transferId, i, chunk);
                        }
                        window.AndroidBlobDownloader.finishChunkedDownload(transferId);
                    } else if (window.AndroidBlobDownloader.processBase64) {
                        window.AndroidBlobDownloader.processBase64(base64Data, safeName, safeMime);
                    }
                } catch (err) {
                    console.error('Chunked transfer error:', err);
                    try {
                        window.AndroidBlobDownloader.processBase64(base64Data, safeName, safeMime);
                    } catch (e2) {}
                }
            }

            function processBlobDirectly(blob, customFileName, customMime) {
                if (!blob) return;
                var mime = (blob && blob.type && blob.type.length > 0) ? blob.type : (customMime || 'audio/wav');
                var name = customFileName || (blob && blob.name) || ('GuberStudio_Audio_' + Date.now() + '.wav');
                var reader = new FileReader();
                reader.onloadend = function() {
                    var base64Data = reader.result;
                    if (base64Data) {
                        sendBase64ToNative(base64Data, name, mime);
                    }
                };
                reader.readAsDataURL(blob);
            }

            function convertBlobToBase64AndSave(blobUrl, customFileName, customMime) {
                if (!blobUrl) return;
                // Check if blob is cached in our Map
                if (window.__guber_blob_map && window.__guber_blob_map.has(blobUrl)) {
                    var cachedBlob = window.__guber_blob_map.get(blobUrl);
                    if (cachedBlob) {
                        processBlobDirectly(cachedBlob, customFileName, customMime);
                        return;
                    }
                }

                // Fallback to fetch
                fetch(blobUrl)
                    .then(function(res) {
                        return res.blob();
                    })
                    .then(function(blob) {
                        processBlobDirectly(blob, customFileName, customMime);
                    })
                    .catch(function(err) {
                        console.error('Error fetching blob download:', err);
                    });
            }

            // Universal Save As polyfill (FileSaver.js compatibility)
            window.saveAs = function(blobOrUrl, name, opts) {
                var filename = name || (blobOrUrl && blobOrUrl.name) || 'audio_file.wav';
                if (blobOrUrl instanceof Blob || blobOrUrl instanceof File) {
                    processBlobDirectly(blobOrUrl, filename, blobOrUrl.type || 'audio/wav');
                    return;
                }
                if (typeof blobOrUrl === 'string') {
                    if (blobOrUrl.startsWith('blob:')) {
                        convertBlobToBase64AndSave(blobOrUrl, filename, '');
                    } else if (blobOrUrl.startsWith('data:')) {
                        sendBase64ToNative(blobOrUrl, filename, '');
                    } else if (blobOrUrl.startsWith('http')) {
                        if (window.AndroidBlobDownloader && window.AndroidBlobDownloader.triggerHttpDownload) {
                            window.AndroidBlobDownloader.triggerHttpDownload(blobOrUrl, filename);
                        }
                    }
                }
            };
            window.FileSaver = { saveAs: window.saveAs };

            // Microsoft Blob Save API polyfills
            if (window.navigator) {
                window.navigator.msSaveBlob = function(blob, filename) {
                    window.saveAs(blob, filename);
                    return true;
                };
                window.navigator.msSaveOrOpenBlob = function(blob, filename) {
                    window.saveAs(blob, filename);
                    return true;
                };
            }

            // Modern File System Access API polyfill (showSaveFilePicker)
            window.showSaveFilePicker = async function(options) {
                var suggestedName = (options && options.suggestedName) ? options.suggestedName : 'download_output.wav';
                return {
                    createWritable: async function() {
                        var chunks = [];
                        return {
                            write: async function(data) {
                                if (data instanceof Blob || data instanceof File) {
                                    chunks.push(data);
                                } else if (data instanceof ArrayBuffer || ArrayBuffer.isView(data)) {
                                    chunks.push(new Blob([data]));
                                } else if (typeof data === 'string') {
                                    chunks.push(new Blob([data]));
                                }
                            },
                            close: async function() {
                                var finalBlob = new Blob(chunks, { type: 'audio/wav' });
                                processBlobDirectly(finalBlob, suggestedName, 'audio/wav');
                            },
                            abort: async function() {}
                        };
                    }
                };
            };

            // Intercept all clicks on download links or buttons
            document.addEventListener('click', function(e) {
                var target = e.target ? e.target.closest('a, button, [role="button"], [download], [aria-label*="download" i], [aria-label*="unduh" i], [aria-label*="wav" i], [aria-label*="suara" i]') : null;
                if (target) {
                    var href = target.href || target.getAttribute('href') || target.src || '';
                    var downloadAttr = target.getAttribute('download') || target.download || '';
                    if (href.startsWith('blob:')) {
                        e.preventDefault();
                        e.stopPropagation();
                        convertBlobToBase64AndSave(href, downloadAttr, '');
                    } else if (href.startsWith('data:')) {
                        e.preventDefault();
                        e.stopPropagation();
                        sendBase64ToNative(href, downloadAttr, '');
                    } else if (downloadAttr && href.startsWith('http')) {
                        e.preventDefault();
                        e.stopPropagation();
                        if (window.AndroidBlobDownloader && window.AndroidBlobDownloader.triggerHttpDownload) {
                            window.AndroidBlobDownloader.triggerHttpDownload(href, downloadAttr);
                        }
                    }
                }
            }, true);

            // Hook HTMLAnchorElement.prototype.click to catch programmatic clicks from voice generators and editors
            var originalAnchorClick = HTMLAnchorElement.prototype.click;
            HTMLAnchorElement.prototype.click = function() {
                var href = this.href || this.getAttribute('href') || '';
                var downloadAttr = this.getAttribute('download') || this.download || '';
                if (href.startsWith('blob:')) {
                    convertBlobToBase64AndSave(href, downloadAttr, '');
                    return;
                } else if (href.startsWith('data:')) {
                    sendBase64ToNative(href, downloadAttr, '');
                    return;
                } else if (downloadAttr && href.startsWith('http')) {
                    if (window.AndroidBlobDownloader && window.AndroidBlobDownloader.triggerHttpDownload) {
                        window.AndroidBlobDownloader.triggerHttpDownload(href, downloadAttr);
                        return;
                    }
                }
                return originalAnchorClick.apply(this, arguments);
            };

            // Hook EventTarget.prototype.dispatchEvent for synthetic click triggers
            var origDispatch = EventTarget.prototype.dispatchEvent;
            EventTarget.prototype.dispatchEvent = function(event) {
                if (event && event.type === 'click' && (this instanceof HTMLAnchorElement)) {
                    var href = this.href || this.getAttribute('href') || '';
                    var downloadAttr = this.getAttribute('download') || this.download || '';
                    if (href.startsWith('blob:')) {
                        convertBlobToBase64AndSave(href, downloadAttr, '');
                        return false;
                    } else if (href.startsWith('data:')) {
                        sendBase64ToNative(href, downloadAttr, '');
                        return false;
                    }
                }
                return origDispatch.apply(this, arguments);
            };

            // Hook window.open for blob: or data: audio downloads
            var origOpen = window.open;
            window.open = function(url) {
                if (url && (typeof url === 'string')) {
                    if (url.startsWith('blob:')) {
                        convertBlobToBase64AndSave(url, 'audio.wav', 'audio/wav');
                        return null;
                    } else if (url.startsWith('data:')) {
                        sendBase64ToNative(url, 'download', '');
                        return null;
                    }
                }
                return origOpen.apply(this, arguments);
            };

            // Also expose global helper for direct JS calls if used by web apps
            window.saveBlobViaAndroid = convertBlobToBase64AndSave;
            window.processBlobDirectlyViaAndroid = processBlobDirectly;
        })();
    """.trimIndent()
    webView?.evaluateJavascript(js, null)
}

/**
 * Dispatches a verified DownloadManager request for HTTP/HTTPS downloads,
 * fully resolving dynamic filename, Content-Disposition, MIME types, and destination dirs.
 */
fun executeDownloadManagerRequest(
    context: Context,
    webView: WebView?,
    url: String,
    userAgent: String,
    contentDisposition: String,
    mimetype: String
) {
    try {
        if (url.startsWith("blob:")) {
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val guessedName = parseFileNameFromContentDisposition(contentDisposition)
                ?: parseFileNameFromUrl(url)
                ?: "suara_tts_$timestamp.wav"
            val js = """
                (function() {
                    var blobUrl = '$url';
                    if (window.__guber_blob_map && window.__guber_blob_map.has(blobUrl)) {
                        var b = window.__guber_blob_map.get(blobUrl);
                        if (b && window.processBlobDirectlyViaAndroid) {
                            window.processBlobDirectlyViaAndroid(b, '$guessedName', 'audio/wav');
                            return;
                        }
                    }
                    fetch(blobUrl)
                        .then(function(res) { return res.blob(); })
                        .then(function(b) {
                            if (window.processBlobDirectlyViaAndroid) {
                                window.processBlobDirectlyViaAndroid(b, '$guessedName', 'audio/wav');
                            }
                        })
                        .catch(function(err) {});
                })();
            """.trimIndent()
            webView?.evaluateJavascript(js, null)
            return
        }

        if (url.startsWith("data:")) {
            saveBase64DataUri(context, url, contentDisposition, mimetype)
            return
        }

        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
        var fileName = parseFileNameFromContentDisposition(contentDisposition)
            ?: parseFileNameFromUrl(url)
            ?: "suara_tts_$timestamp.wav"
        if (!fileName.endsWith(".wav", true)) {
            fileName = "$fileName.wav"
        }
        val sanitizedName = sanitizeFileName(fileName)

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setMimeType("audio/wav")
            val cookies = CookieManager.getInstance().getCookie(url)
            if (!cookies.isNullOrEmpty()) addRequestHeader("cookie", cookies)
            if (userAgent.isNotBlank()) addRequestHeader("User-Agent", userAgent)
            val currentWebUrl = webView?.url
            if (!currentWebUrl.isNullOrBlank()) addRequestHeader("Referer", currentWebUrl)

            setTitle(sanitizedName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, sanitizedName)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        downloadManager?.enqueue(request)
    } catch (_: Exception) {}
}

private fun saveBase64DataUri(
    context: Context,
    dataUri: String,
    suggestedFileName: String,
    defaultMime: String
) {
    try {
        if (dataUri.isBlank()) return

        val dataString = if (dataUri.startsWith("data:") && dataUri.contains(",")) {
            dataUri.substring(dataUri.indexOf(",") + 1)
        } else {
            dataUri.trim()
        }

        val bytes = Base64.decode(dataString, Base64.DEFAULT)
        if (bytes.isEmpty()) return

        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
        val fallbackName = "suara_tts_$timestamp.wav"

        var cleanName = parseFileNameFromContentDisposition(suggestedFileName) ?: suggestedFileName
        if (cleanName.isBlank() || cleanName == "download" || cleanName.startsWith("blob:")) {
            cleanName = fallbackName
        }
        if (!cleanName.endsWith(".wav", true)) {
            cleanName = "$cleanName.wav"
        }
        val sanitizedName = sanitizeFileName(cleanName)
        val resolvedMime = "audio/wav"

        var savedSuccessfully = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collectionUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val relativeDir = Environment.DIRECTORY_DOWNLOADS

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, sanitizedName)
                put(MediaStore.MediaColumns.MIME_TYPE, resolvedMime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativeDir)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            try {
                val uri = context.contentResolver.insert(collectionUri, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                    savedSuccessfully = true
                }
            } catch (_: Exception) {}
        }

        if (!savedSuccessfully) {
            val baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!baseDir.exists()) baseDir.mkdirs()
            val file = File(baseDir, sanitizedName)
            FileOutputStream(file).use { it.write(bytes) }
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(resolvedMime)) { _, _ -> }
            savedSuccessfully = true
        }

        if (savedSuccessfully) {
            notifyDownloadCompleted(context, sanitizedName, resolvedMime, sanitizedName)
        } else {
            handleDownloadFailed()
        }
    } catch (_: Exception) {
        handleDownloadFailed()
    }
}

internal var pendingShopeeUrl: String? = null

fun notifyDownloadCompleted(context: Context, fileName: String, mimeType: String, pathOrUri: String) {
    val resolvedMime = resolveMimeType(fileName, mimeType, pathOrUri)
    val downloadType = getDownloadType(fileName, resolvedMime)
    val loc = getStorageLocationLabel(downloadType)

    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, "Download selesai. File tersimpan di folder Downloads", Toast.LENGTH_LONG).show()
        onDownloadFinishedListener?.invoke(
            DownloadSuccessEvent(
                fileName = fileName,
                type = downloadType,
                locationLabel = loc
            )
        )

        val urlToOpen = pendingShopeeUrl
        pendingShopeeUrl = null
        if (!urlToOpen.isNullOrBlank()) {
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }, 5000)
        }
    }
}

fun handleDownloadFailed() {
    pendingShopeeUrl = null
}

/**
 * JavaScript interface for handling in-page image, voice/audio, document, and blob download callbacks.
 */
class BlobDownloadInterface(
    private val context: Context,
    private val webViewProvider: () -> WebView?,
    private val downloadTrigger: ((String, String, String, String) -> Unit)? = null
) {
    private class ChunkedTransferSession(
        val fileName: String,
        val mimeType: String,
        val totalChunks: Int,
        val totalBytes: Long
    ) {
        val chunks = java.util.concurrent.ConcurrentHashMap<Int, String>()
    }

    private val chunkTransfers = java.util.concurrent.ConcurrentHashMap<String, ChunkedTransferSession>()

    @JavascriptInterface
    fun startChunkedDownload(transferId: String, fileName: String, mimeType: String, totalChunks: Int, totalBytes: Long) {
        chunkTransfers[transferId] = ChunkedTransferSession(fileName, mimeType, totalChunks, totalBytes)
    }

    @JavascriptInterface
    fun appendDownloadChunk(transferId: String, chunkIndex: Int, chunkData: String) {
        val session = chunkTransfers[transferId] ?: return
        session.chunks[chunkIndex] = chunkData
    }

    @JavascriptInterface
    fun finishChunkedDownload(transferId: String) {
        val session = chunkTransfers.remove(transferId) ?: return
        try {
            val sb = java.lang.StringBuilder()
            for (i in 0 until session.totalChunks) {
                val chunk = session.chunks[i] ?: ""
                sb.append(chunk)
            }
            val completeData = sb.toString()
            saveBase64DataUri(context, completeData, session.fileName, session.mimeType)
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Gagal memproses audio: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    @JavascriptInterface
    fun processBase64(base64Data: String, fileName: String, mimeType: String) {
        saveBase64DataUri(context, base64Data, fileName, mimeType)
    }

    @JavascriptInterface
    fun triggerHttpDownload(url: String, fileName: String) {
        val wv = webViewProvider()
        Handler(Looper.getMainLooper()).post {
            val ua = wv?.settings?.userAgentString ?: ""
            val cd = if (fileName.isNotBlank()) "attachment; filename=\"$fileName\"" else ""
            if (downloadTrigger != null) {
                downloadTrigger.invoke(url, ua, cd, "")
            } else {
                executeDownloadManagerRequest(context, wv, url, ua, cd, "")
            }
        }
    }
}
