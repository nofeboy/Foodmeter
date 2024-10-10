package com.example.foodmeter.ui.result

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodmeter.MainActivity
import com.example.foodmeter.ui.capture.CaptureActivity

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 액션바 제목 설정 및 중앙 정렬
        supportActionBar?.apply {
            title = "식재료 분석 결과"
            setDisplayShowTitleEnabled(true) // 제목을 표시
            displayOptions = androidx.appcompat.app.ActionBar.DISPLAY_SHOW_TITLE
            setDisplayShowTitleEnabled(false) // 기본 제목 비활성화
            customView = TextView(this@ResultActivity).apply {
                text = "식재료 분석 결과"
                textSize = 20f
                gravity = Gravity.CENTER
                layoutParams = androidx.appcompat.app.ActionBar.LayoutParams(
                    androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT,
                    androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            }
            setDisplayShowCustomEnabled(true) // 커스텀 뷰 활성화
        }

        // 메인 레이아웃 설정
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16) // 패딩 추가
        }

        // Intent로부터 Uri 데이터를 받아옴
        val photoUri = intent.getStringExtra("photoUri")?.let { Uri.parse(it) }

        // 사진 이미지뷰 추가
        val imageView = ImageView(this).apply {
            if (photoUri != null) {
                setImageURI(photoUri) // Uri로 받은 사진을 이미지뷰에 설정
            } else {
                // Uri가 없을 경우 처리
                val errorMessage = "사진을 불러오는데 실패했습니다."
                Toast.makeText(this@ResultActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1000 // 화면에 맞게 적절한 크기 설정
            )
        }

        // 등급 표시 텍스트뷰
        val gradeText = TextView(this).apply {
            // 추후 등급 분석 데이터를 받아서 적용할 수 있도록 여기를 변경
            text = "등급: 1++ 등급.\n마블링이 보기 좋네요!"
            textSize = 24f
            setPadding(0, 35, 0, 45) // 위쪽과 아래쪽에 24dp 패딩 추가
        }

        // 홈으로 가기 버튼 추가
        val homeButton = Button(this).apply {
            text = "홈으로 가기"
            setOnClickListener {
                // 홈(MainActivity)으로 이동
                val intent = Intent(this@ResultActivity, MainActivity::class.java)
                startActivity(intent)
                finish() // 현재 액티비티 종료
            }
        }

        // 다시 찍기 버튼 추가
        val backButton = Button(this).apply {
            text = "다시 찍기"
            setOnClickListener {
                // CaptureActivity로 다시 이동
                val intent = Intent(this@ResultActivity, CaptureActivity::class.java)
                startActivity(intent)
                finish() // 현재 액티비티 종료
            }
        }

        // 레이아웃에 추가
        mainLayout.addView(imageView)
        mainLayout.addView(gradeText)
        mainLayout.addView(homeButton)
        mainLayout.addView(backButton)

        setContentView(mainLayout)
    }

}
