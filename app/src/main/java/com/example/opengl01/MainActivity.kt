package com.example.opengl01

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val openGLView = findViewById<OpenGLView>(R.id.glview)

        findViewById<Button>(R.id.plusX).setOnClickListener {
            openGLView.renderer.translateCamera(1f, 0f, 0f)
        }
        findViewById<Button>(R.id.minusX).setOnClickListener {
            openGLView.renderer.translateCamera(-1f, 0f, 0f)
        }
        findViewById<Button>(R.id.plusY).setOnClickListener {
            openGLView.renderer.translateCamera(0f, 1f, 0f)
        }
        findViewById<Button>(R.id.minusY).setOnClickListener {
            openGLView.renderer.translateCamera(0f, -1f, 0f)
        }
        findViewById<Button>(R.id.plusZ).setOnClickListener {
            openGLView.renderer.translateCamera(0f, 0f, 1f)
        }
        findViewById<Button>(R.id.minusZ).setOnClickListener {
            openGLView.renderer.translateCamera(0f, 0f, -1f)
        }
        findViewById<Button>(R.id.rotateClockwise).setOnClickListener {
            openGLView.renderer.rotateCamera(-10f)
        }
        findViewById<Button>(R.id.rotateAnticlockwise).setOnClickListener {
            openGLView.renderer.rotateCamera(10f)
        }
        findViewById<Button>(R.id.cameraForward).setOnClickListener {
            openGLView.renderer.moveCamera(1.0f)
        }
        findViewById<Button>(R.id.cameraBack).setOnClickListener {
            openGLView.renderer.moveCamera(-1.0f)
        }
    }
}