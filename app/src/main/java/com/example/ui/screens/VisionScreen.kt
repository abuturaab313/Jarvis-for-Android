package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*
import com.example.utils.glassmorphic
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.util.concurrent.Executor

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VisionScreen(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val visionResult by viewModel.visionResult.collectAsState()
    val isAiProcessing by viewModel.isAiProcessing.collectAsState()

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 80.dp)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(cornerRadius = 16.dp)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VISION HUD MATRIX",
                        color = NeonCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "CAMERAX / OCR / SCENE RECOGNITION",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = "Vision Target",
                    tint = GlowingGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Camera Viewport / Holographic Target Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .glassmorphic(cornerRadius = 20.dp, borderColor = NeonCyan)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (cameraPermissionState.status.isGranted) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = androidx.camera.core.Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                imageCapture = ImageCapture.Builder().build()
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner, cameraSelector, preview, imageCapture
                                    )
                                } catch (e: Exception) {
                                    // Camera bind error
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Target Crosshair Overlay
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .border(width = 1.dp, color = NeonCyan, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterCenterFocus,
                            contentDescription = "Targeting",
                            tint = NeonCyan.copy(alpha = 0.8f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera Permission Needed",
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "CAMERA PERMISSION REQUIRED",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSpaceBackground),
                            modifier = Modifier.testTag("btn_grant_camera")
                        ) {
                            Text("GRANT CAMERA ACCESS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trigger Capture / Analyze Button
            Button(
                onClick = {
                    takePhotoAndAnalyze(context, imageCapture) { bitmap ->
                        viewModel.analyzeImageWithGemini(bitmap)
                    }
                },
                enabled = !isAiProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSpaceBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_capture_vision")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = "Scan")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAiProcessing) "NEURAL MATRIX ANALYZING..." else "CAPTURE & ANALYZE WITH GEMINI",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Analysis Results Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .glassmorphic(cornerRadius = 16.dp)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column {
                    Text(
                        text = "NEURAL VISION REPORT",
                        color = GlowingGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = visionResult.summary.ifEmpty { "Standing by for target frame capture. Align object inside crosshairs and press analyze." },
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

private fun takePhotoAndAnalyze(
    context: Context,
    imageCapture: ImageCapture?,
    onBitmapCaptured: (Bitmap) -> Unit
) {
    if (imageCapture == null) {
        // Fallback simulation bitmap if camera capture isn't initialized
        val mockBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        onBitmapCaptured(mockBitmap)
        return
    }

    val photoFile = File(context.cacheDir, "jarvis_vision_temp.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                if (bitmap != null) {
                    onBitmapCaptured(bitmap)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                val mockBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                onBitmapCaptured(mockBitmap)
            }
        }
    )
}
