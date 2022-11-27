package com.example.opengl01

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLRenderer(val textureAvailableCallback: (SurfaceTexture) -> Unit) : GLSurfaceView.Renderer {

    val vertexShaderSrc =
        "attribute vec4 aVertex;\n" +
                "uniform mat4 uView, uProjection;\n" +
                "void main(void)\n" +
                "{\n" +
                "gl_Position = uProjection * uView * aVertex;\n" +
                "}\n"

    val fragmentShaderSrc =
        "precision mediump float;\n" +
                "uniform vec4 uColour;\n" +
                "void main(void)\n" +
                "{\n" +
                "gl_FragColor = uColour;\n" +
                "}\n"

    // Must negate y when calculating texcoords from vertex coords as bitmap image data assumes
    // y increases downwards
    val texVertexShaderSrc =
        "attribute vec4 aVertex;\n" +
                "varying vec2 vTextureValue;\n" +
                "void main (void)\n" +
                "{\n" +
                "gl_Position = aVertex;\n" +
                "vTextureValue = vec2(0.5*(1.0 + aVertex.x), 0.5*(1.0-aVertex.y));\n" +
                "}\n"
    val texFragmentShaderSrc =
        "#extension GL_OES_EGL_image_external: require\n" +
                "precision mediump float;\n" +
                "varying vec2 vTextureValue;\n" +
                "uniform samplerExternalOES uTexture;\n" +
                "void main(void)\n" +
                "{\n" +
                "gl_FragColor = texture2D(uTexture,vTextureValue);\n" +
                "}\n"

    var shaderProgram = -1
    var texShaderProgram = -1


    var fbuf: FloatBuffer? = null
    var fbuf2: FloatBuffer? = null
    var texBuffer: FloatBuffer? = null

    lateinit var ibuf: ShortBuffer
    lateinit var texIndexBuffer: ShortBuffer

    var viewMatrix = FloatArray(16)
    var projectionMatrix = FloatArray(16)

    var cameraPos = FloatArray(3)
    var cameraRotation = 0f



    // We initialise the OpenGL view here
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background colour (red=0, green=0, blue=0, alpha=1)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // Enable depth testing - will cause nearer 3D objects to automatically
        // be drawn over further objects
        GLES20.glClearDepthf(1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // http://stackoverflow.com/questions/6414003/using-surfacetexture-in-android
        val GL_TEXTURE_EXTERNAL_OES = 0x8d65
        val textureId = IntArray(1)
        GLES20.glGenTextures(1, textureId, 0)
        if (textureId[0] != 0) {
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId[0])
            GLES20.glTexParameteri(
                GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST
            )
            GLES20.glTexParameteri(
                GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST
            )

            val cameraFeedSurfaceTexture = SurfaceTexture(textureId[0])
            // Compile and link the shaders
            val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSrc)
            val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSrc)
            shaderProgram = linkShader(vertexShader, fragmentShader)

            val texVertexShader = compileShader(GLES20.GL_VERTEX_SHADER, texVertexShaderSrc)
            val texFragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, texFragmentShaderSrc)
            texShaderProgram = linkShader(texVertexShader, texFragmentShader)

            // Define the vertices we want to draw, and make a buffer from them
            val vertices = floatArrayOf(
                0f, 0f, -3f,
                1f, 0f, -3f,
                0.5f, 1f, -3f,
                -0.5f, 0f, -6f,
                0.5f, 0f, -6f,
                0f, 1f, -6f
            )


            val square = floatArrayOf(
                -1f, 0f, -2f,
                0f, 0f, -2f,
                0f, 1f, -2f,
                -1f, 1f, -2f
            )

            val indices = shortArrayOf(0, 1, 2, 2, 3, 0)


            fbuf = makeBuffer(vertices)
            fbuf2 = makeBuffer(square)
            ibuf = makeIndexBuffer(indices)

            createCameraRect()

            textureAvailableCallback(cameraFeedSurfaceTexture)
        }
    }

    // We draw our shapes here
    override fun onDrawFrame(unused: GL10) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.rotateM(viewMatrix, 0, -cameraRotation, 0f, 1f, 0f)
        Matrix.translateM(
            viewMatrix, 0,
            -cameraPos[0], -cameraPos[1], -cameraPos[2]
        )


        // Check we have a valid shader program and buffer
        if (true) { //shaderProgram > 0 && fbuf != null) {

            GLES20.glUseProgram(shaderProgram)
            // Create a reference to the attribute shader variable aVertex
            val ref_aVertex = GLES20.glGetAttribLocation(shaderProgram, "aVertex")

            // Enable it
            GLES20.glEnableVertexAttribArray(ref_aVertex)


            val ref_uViewMatrix = GLES20.glGetUniformLocation(shaderProgram, "uView")
            GLES20.glUniformMatrix4fv(ref_uViewMatrix, 1, false, viewMatrix, 0);


            val ref_uProjMatrix = GLES20.glGetUniformLocation(shaderProgram, "uProjection")
            GLES20.glUniformMatrix4fv(ref_uProjMatrix, 1, false, projectionMatrix, 0);


            // Create a reference to the uniform shader variable uColour
            val ref_uColour = GLES20.glGetUniformLocation(shaderProgram, "uColour")

            // Specify format of data in buffer

            GLES20.glVertexAttribPointer(ref_aVertex, 3, GLES20.GL_FLOAT, false, 0, fbuf2)
            // Send red colour to the shader
            val red = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)
            val blue = floatArrayOf(0.0f, 0.0f, 1.0f, 1.0f)


            GLES20.glUniform4fv(ref_uColour, 1, blue, 0)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, ibuf.limit(), GLES20.GL_UNSIGNED_SHORT, ibuf)


            GLES20.glVertexAttribPointer(ref_aVertex, 3, GLES20.GL_FLOAT, false, 0, fbuf)
            // Draw first triangle using first 3 vertices in buffer
            GLES20.glUniform4fv(ref_uColour, 1, red, 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)

            // Send yellow colour to the shader

            val yellow = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f)
            GLES20.glUniform4fv(ref_uColour, 1, yellow, 0)

            // Draw second triangle using vertices 3-5 in buffer
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 3, 3)

        }
    }

    // Used if the screen is resized
    override fun onSurfaceChanged(unused: GL10, w: Int, h: Int) {
        GLES20.glViewport(0, 0, w, h)
        val hfov = 60.0f
        val aspect: Float = w.toFloat() / h
        Matrix.perspectiveM(projectionMatrix, 0, hfov / aspect, aspect, 0.001f, 100f)

    }

    fun compileShader(shaderType: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(shaderType)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    fun linkShader(vertexShader: Int, fragmentShader: Int): Int {
        val shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            Log.e(
                "OpenGL01Log",
                "Error linking shader program: " + GLES20.glGetProgramInfoLog(shaderProgram)
            )
            GLES20.glDeleteProgram(shaderProgram)
            return -1
        }
        GLES20.glUseProgram(shaderProgram)
        Log.d("OpenGL01Log", "Shader program = $shaderProgram")
        return shaderProgram
    }

    fun makeBuffer(vertices: FloatArray): FloatBuffer {
        val bbuf: ByteBuffer = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES)
        bbuf.order(ByteOrder.nativeOrder())
        val fbuf: FloatBuffer = bbuf.asFloatBuffer()
        fbuf.put(vertices)
        fbuf.position(0)
        return fbuf
    }

    fun makeIndexBuffer(indices: ShortArray): ShortBuffer {
        val bbuf: ByteBuffer = ByteBuffer.allocateDirect(indices.size * Short.SIZE_BYTES)
        bbuf.order(ByteOrder.nativeOrder())
        val sbuf: ShortBuffer = bbuf.asShortBuffer()
        sbuf.put(indices)
        sbuf.position(0)
        return sbuf
    }

    private fun createCameraRect() {

        val cameraRect = floatArrayOf(-1f, 1f, 0f, -1f, -1f, 0f, 1f, -1f, 0f, 1f, 1f, 0f)
        val indices = shortArrayOf(0, 1, 2, 2, 3, 0)

        texBuffer = makeBuffer(cameraRect)
        texIndexBuffer = makeIndexBuffer(indices)
    }

    fun translateCamera(dx: Float, dy: Float, dz: Float) {
        cameraPos[0] += dx
        cameraPos[1] += dy
        cameraPos[2] += dz
    }

    fun rotateCamera(degrees: Float) {
        cameraRotation += degrees
        if (cameraRotation > 180f) {
            cameraRotation -= 360f
        }
        if (cameraRotation < -180f) {
            cameraRotation += 360f
        }
    }

    fun moveCamera(d: Float) {
        val rad = cameraRotation * (Math.PI / 180)
        cameraPos[0] += (-d * Math.sin(rad)).toFloat()
        cameraPos[2] += (-d * Math.cos(rad)).toFloat()
    }
}