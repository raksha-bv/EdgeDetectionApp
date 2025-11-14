package com.example.edgedetectionapp

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer : GLSurfaceView.Renderer {
    companion object {
        private const val TAG = "GLRenderer"
    }
    private var program: Int = 0
    private var vertexBuffer: FloatBuffer
    private var textureId: Int = 0
    private var currentEffect = EffectType.NORMAL
    private var bitmap: Bitmap? = null
    private var needsTextureUpdate = false

    enum class EffectType {
        NORMAL, GRAYSCALE, INVERT, SEPIA, BLUR
    }

    private val vertices = floatArrayOf(
        -1.0f, -1.0f, 0.0f, 0.0f,
         1.0f, -1.0f, 1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f, 1.0f,
         1.0f,  1.0f, 1.0f, 1.0f
    )

    private val vertexShaderCode = """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        void main() {
            gl_Position = aPosition;
            vTexCoord = aTexCoord;
        }
    """

    private fun getFragmentShaderCode(effect: EffectType): String {
        return when (effect) {
            EffectType.NORMAL -> """
                precision mediump float;
                varying vec2 vTexCoord;
                uniform sampler2D uTexture;
                void main() {
                    gl_FragColor = texture2D(uTexture, vTexCoord);
                }
            """
            EffectType.GRAYSCALE -> """
                precision mediump float;
                varying vec2 vTexCoord;
                uniform sampler2D uTexture;
                void main() {
                    vec4 color = texture2D(uTexture, vTexCoord);
                    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
                    gl_FragColor = vec4(gray, gray, gray, color.a);
                }
            """
            EffectType.INVERT -> """
                precision mediump float;
                varying vec2 vTexCoord;
                uniform sampler2D uTexture;
                void main() {
                    vec4 color = texture2D(uTexture, vTexCoord);
                    gl_FragColor = vec4(1.0 - color.rgb, color.a);
                }
            """
            EffectType.SEPIA -> """
                precision mediump float;
                varying vec2 vTexCoord;
                uniform sampler2D uTexture;
                void main() {
                    vec4 color = texture2D(uTexture, vTexCoord);
                    float r = dot(color.rgb, vec3(0.393, 0.769, 0.189));
                    float g = dot(color.rgb, vec3(0.349, 0.686, 0.168));
                    float b = dot(color.rgb, vec3(0.272, 0.534, 0.131));
                    gl_FragColor = vec4(r, g, b, color.a);
                }
            """
            EffectType.BLUR -> """
                precision mediump float;
                varying vec2 vTexCoord;
                uniform sampler2D uTexture;
                void main() {
                    vec2 texelSize = vec2(1.0/512.0, 1.0/512.0);
                    vec4 color = vec4(0.0);
                    for(int i = -2; i <= 2; i++) {
                        for(int j = -2; j <= 2; j++) {
                            color += texture2D(uTexture, vTexCoord + vec2(float(i), float(j)) * texelSize);
                        }
                    }
                    gl_FragColor = color / 25.0;
                }
            """
        }
    }

    init {
        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        createShaderProgram()
        
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        
        if (needsTextureUpdate && bitmap != null && !bitmap!!.isRecycled) {
            Log.d(TAG, "Uploading texture to GPU")
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            needsTextureUpdate = false
        } else if (needsTextureUpdate && bitmap != null && bitmap!!.isRecycled) {
            Log.e(TAG, "Cannot upload texture - bitmap is recycled")
            needsTextureUpdate = false
        }
        
        if (bitmap != null) {
            GLES20.glUseProgram(program)
        
        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        val textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)
        
        vertexBuffer.position(2)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)
        
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            
            GLES20.glDisableVertexAttribArray(positionHandle)
            GLES20.glDisableVertexAttribArray(texCoordHandle)
        } else {
            Log.w(TAG, "No bitmap available for rendering")
        }
    }

    fun cleanup() {
        bitmap?.recycle()
        bitmap = null
    }

    private fun createShaderProgram() {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShaderCode(currentEffect))
        
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    fun setEffect(effect: EffectType) {
        if (currentEffect != effect) {
            Log.d(TAG, "Changing effect from $currentEffect to $effect")
            currentEffect = effect
            createShaderProgram()
        }
    }

    fun updateTexture(bitmap: Bitmap) {
        Log.d(TAG, "Updating texture with bitmap: ${bitmap.width}x${bitmap.height}")
        // Create a copy to avoid recycling issues on different threads
        if (!bitmap.isRecycled) {
            this.bitmap?.recycle() // Clean up previous bitmap
            this.bitmap = bitmap.copy(bitmap.config, false)
            needsTextureUpdate = true
        } else {
            Log.w(TAG, "Cannot update texture - bitmap is already recycled")
        }
    }
}