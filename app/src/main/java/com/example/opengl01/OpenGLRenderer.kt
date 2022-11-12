package com.example.opengl01

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLRenderer : GLSurfaceView.Renderer {

    val vertexShaderSrc =
        "attribute vec4 aVertex;\n" +
                "uniform mat4 uView, uProjection;\n" +
                "void main(void)\n" +
                "{\n"+
                "gl_Position = uProjection * uView * aVertex;\n" +
                "}\n"

    val fragmentShaderSrc =
        "precision mediump float;\n" +
                "uniform vec4 uColour;\n" +
                "void main(void)\n"+
                "{\n"+
                "gl_FragColor = uColour;\n" +
                "}\n"

    var shaderProgram = -1

    var fbuf: FloatBuffer? = null

    var viewMatrix = FloatArray(16)
    var projectionMatrix = FloatArray(16)

    // We initialise the OpenGL view here
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background colour (red=0, green=0, blue=0, alpha=1)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // Enable depth testing - will cause nearer 3D objects to automatically
        // be drawn over further objects
        GLES20.glClearDepthf(1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Compile and link the shaders
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSrc)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSrc)
        shaderProgram = linkShader(vertexShader, fragmentShader)

        // Define the vertices we want to draw, and make a buffer from them
        val vertices = floatArrayOf( 0f,0f,-3f,  1f,0f,-3f,  0.5f,1f,-3f,
            -0.5f,0f,-6f,  0.5f,0f,-6f,  0f,1f,-6f)

        // verices with colours
        val vertices2 = floatArrayOf(
            0f,0f,-3f,  1f,0f,0f,
            1f,0f,-3f,  0f,1f,0f,
            0.5f,1f,-3f, 0f,0f,1f,
            -0.5f,0f,-6f, 0f,1f,0f,
            0.5f,0f,-6f,  0f,1f,0f,
            0f,1f,-6f, 0f,1f,0f)


        fbuf = makeBuffer(vertices2)
    }

    // We draw our shapes here
    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.translateM(viewMatrix, 0, 0f, 0f, 2f)

        // Check we have a valid shader program and buffer
        if(shaderProgram > 0 && fbuf != null) {
            // Create a reference to the attribute shader variable aVertex
            val ref_aVertex = GLES20.glGetAttribLocation(shaderProgram,"aVertex")

            // Enable it
            GLES20.glEnableVertexAttribArray(ref_aVertex)

            val ref_uViewMatrix = GLES20.glGetUniformLocation(shaderProgram,"uView")
            GLES20.glUniformMatrix4fv (ref_uViewMatrix, 1, false, viewMatrix, 0);

            val ref_uProjMatrix = GLES20.glGetUniformLocation(shaderProgram,"uProjection")
            GLES20.glUniformMatrix4fv (ref_uProjMatrix, 1, false, projectionMatrix, 0);

            // Create a reference to the uniform shader variable uColour
            val ref_uColour = GLES20.glGetUniformLocation(shaderProgram,"uColour")

            // Specify format of data in buffer
            GLES20.glVertexAttribPointer(ref_aVertex, 3, GLES20.GL_FLOAT, false, 24, fbuf)

            // Send red colour to the shader
            val red = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)
            GLES20.glUniform4fv (ref_uColour, 1, red, 0)

            // Draw first triangle using first 3 vertices in buffer
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)

            // Send yellow colour to the shader
            val yellow = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f)
            GLES20.glUniform4fv (ref_uColour, 1, yellow, 0)

            // Draw second triangle using vertices 3-5 in buffer
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 3, 3)
        }
    }

    // Used if the screen is resized
    override fun onSurfaceChanged(unused: GL10, w: Int, h: Int) {
        GLES20.glViewport(0, 0, w, h)
        val hfov = 60.0f
        val aspect : Float = w.toFloat()/h
        Matrix.perspectiveM(projectionMatrix, 0, hfov/aspect ,aspect, 0.001f, 100f)

    }

    fun compileShader(shaderType: Int, shaderCode: String) : Int {
        val shader = GLES20.glCreateShader(shaderType)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    fun linkShader(vertexShader: Int, fragmentShader: Int) : Int{
        val shaderProgram=GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)
        GLES20.glUseProgram(shaderProgram)
        return shaderProgram
    }

    fun makeBuffer(vertices: FloatArray) : FloatBuffer {
        val bbuf : ByteBuffer = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES)
        bbuf.order(ByteOrder.nativeOrder())
        val fbuf : FloatBuffer  = bbuf.asFloatBuffer()
        fbuf.put(vertices)
        fbuf.position(0)
        return fbuf
    }
}