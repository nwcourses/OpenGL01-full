package com.example.opengl01

import android.opengl.GLSurfaceView
import android.content.Context
import android.util.AttributeSet


class OpenGLView(ctx: Context, aset: AttributeSet)  :GLSurfaceView(ctx, aset) {
    // Make the renderer an attribute of the OpenGLView so we can access
    // it from outside the OpenGLView
    val renderer = OpenGLRenderer()
    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
    }
}
