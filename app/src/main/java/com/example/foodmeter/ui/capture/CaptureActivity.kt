package com.example.foodmeter.ui.capture

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.foodmeter.MainActivity
import com.example.foodmeter.ui.result.ResultActivity
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CaptureActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            CameraScreen()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    var hasCameraPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    // 갤러리에서 사진 선택하는 ActivityResultLauncher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                // 사진이 선택되면 결과 화면으로 이동
                val intent = Intent(context, ResultActivity::class.java).apply {
                    putExtra("photoUri", it.toString())
                }
                context.startActivity(intent)
            }
        }
    )

    // 권한 요청
    LaunchedEffect(true) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    //최근 사진 URI 가져오기
    val recentPhotoUri = getLatestPhotoUri(context)

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(7 / 8f) // 화면의 5/6을 카메라 영역으로 설정
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            // ImageCapture 객체 생성
                            imageCapture = ImageCapture.Builder().build()

                            try {
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner, cameraSelector, preview, imageCapture
                                )
                            } catch (exc: Exception) {
                                Toast.makeText(
                                    ctx,
                                    "카메라 바인딩 실패: ${exc.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // 모서리 부분 둥근 사각형 UI
                    CameraFrameOverlay()

                    // 카메라 버튼을 카메라 영역 하단에 배치
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 45.dp), // 버튼과 하단 사이 간격 추가
                        contentAlignment = Alignment.Center
                    ) {

                        CaptureButton(imageCapture)
                    }
                }
            }

        } else {
            Text(
                text = "카메라 권한이 필요합니다.",
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        // 상단 뒤로가기 버튼
        BackButton()

        // 팁 메시지를 위한 하단 1/6 영역을 화면 최하단에 배치
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter) // 팁 메시지를 화면의 최하단에 붙이기
                .background(Color.White), // 팁 메시지 영역 배경
            contentAlignment = Alignment.Center
        ) {
            TipMessage() // 팁 메시지를 하단 박스 안에 배치
        }
    }
}

@Composable
fun CameraFrameOverlay(
    widthFactor: Float = 1.2f,  // 기본값을 1로 설정
    heightFactor: Float = 0.9f  // 기본값을 1로 설정
) {
    // 모서리 부분 둥근 사각형 모양을 위한 레이아웃
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFactor)  // 가로 크기 제어
            .fillMaxHeight(heightFactor) // 세로 크기 제어
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(50.dp)
                .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)) // 둥근 모서리와 투명도 50% 설정
        )
    }
}


@Composable
fun CaptureButton(imageCapture: ImageCapture?) {
    val context = LocalContext.current
    val outputDirectory = context.externalMediaDirs.firstOrNull()?.let { File(it, "CapturedImages") }
    outputDirectory?.mkdirs()

    Box(
        modifier = Modifier
            .size(80.dp) // 원형 버튼 크기 설정
            .clip(CircleShape) // 원형 모양으로 설정
            .background(Color.White) // 배경색을 흰색으로 설정
            .clickable { // 클릭 이벤트 설정
                // 사진 촬영 로직
                imageCapture?.let {
                    val photoFile = File(outputDirectory, "${System.currentTimeMillis()}.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    it.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object :
                        ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Toast.makeText(context, "사진 저장 실패: ${exc.message}", Toast.LENGTH_SHORT).show()
                        }

                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            // 사진이 저장되면 결과 화면으로 이동
                            val photoUri = Uri.fromFile(photoFile)
                            val intent = Intent(context, ResultActivity::class.java).apply {
                                putExtra("photoUri", photoUri.toString())
                            }
                            context.startActivity(intent)
                        }
                    })
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // 빈 텍스트 또는 아이콘을 표시하지 않고 버튼을 원형만 유지
    }
}


@Composable
fun TipMessage() {
    Text(
        text = "이런 경우 정확도가 떨어져요:\n너무 밝거나 어두운 경우, 원색 이외의 조명이 있는 경우,\n식재료가 비닐로 포장된 경우, 화질이 낮은 경우",
        color = Color.Black,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        modifier = Modifier
            .padding(16.dp)
            .background(Color.White) // 하단 하얀 여백에 표시
            .padding(16.dp) // 텍스트 내부 패딩
    )
}

@Composable
fun BackButton() {
    val context = LocalContext.current

    Icon(
        imageVector = Icons.Filled.ArrowBack,
        contentDescription = "뒤로가기",
        tint = Color.White, // 뒤로가기 버튼 색상을 흰색으로 설정
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            }
    )
}

//갤러리 가장 최근 이미지 가져오는 함수
fun getLatestPhotoUri(context: android.content.Context): Uri? {
    val projection = arrayOf(
        android.provider.MediaStore.Images.Media._ID,
        android.provider.MediaStore.Images.Media.DATE_ADDED
    )

    val sortOrder = "${android.provider.MediaStore.Images.Media.DATE_ADDED} DESC"
    val queryUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val cursor = context.contentResolver.query(
        queryUri,
        projection,
        null,
        null,
        sortOrder
    )

    cursor?.use {
        if (it.moveToFirst()) {
            val id = it.getLong(it.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media._ID))
            return Uri.withAppendedPath(queryUri, id.toString())
        }
    }
    return null
}
