package com.example.foodmeter

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.foodmeter.ui.capture.CaptureActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 메인 레이아웃 설정
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.homebackground) // 배경 이미지 설정
        }

        // 로고 이미지 추가 (상단에 위치하면서 좌우 중앙 정렬)
        val logoImage = ImageView(this).apply {
            setImageResource(R.drawable.marbling_studios_logo) // drawable 폴더에 저장된 로고 이미지 사용
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 100 // 상단에서 여유 공간을 주어 로고를 배치
                gravity = Gravity.CENTER_HORIZONTAL // 좌우 중앙 정렬
            }
        }
        mainLayout.addView(logoImage)

        // 빈 공간을 추가해 카메라 버튼을 중앙에 배치하기 위해 여유를 줌
        val space = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1.0f // 가중치로 빈 공간을 채움
        )
        mainLayout.addView(LinearLayout(this), space)

        // 카메라 버튼 이미지 추가 (중앙에 위치)
        val cameraButton = ImageView(this).apply {
            setImageResource(R.drawable.camera_icon) // drawable 폴더에 저장된 카메라 아이콘 사용
            setOnClickListener {
                // 카메라 실행 액티비티로 이동
                val intent = Intent(this@MainActivity, CaptureActivity::class.java)
                startActivity(intent)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER // 카메라 버튼은 중앙에 위치
            }
        }
        mainLayout.addView(cameraButton)

        // 다시 빈 공간 추가 (아래쪽 여유 공간 확보)
        mainLayout.addView(LinearLayout(this), space)

        // 레이아웃 설정
        setContentView(mainLayout)
    }
}
