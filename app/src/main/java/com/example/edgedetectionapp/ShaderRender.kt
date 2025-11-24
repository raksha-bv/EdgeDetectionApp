package com.example.edgedetectionapp

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ShaderRenderer : GLSurfaceView.Renderer {
    
    private val TAG = "ShaderRenderer"
    
    // Shader effect modes
    enum class ShaderEffect {
        NORMAL,
        GRAYSCALE,
        INVERT,
        EDGE_DETECTION
    }
    
    var currentEffect = ShaderEffect.NORMAL
    
    private var program = 0
    private var textureId = 0
    private var vertexBuffer: FloatBuffer? = null
    private var textureBuffer: FloatBuffer? = null
    
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var samplerHandle = 0
    private var effectHandle = 0
    
    // Vertex shader
    private val vertexShaderCode = """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        
        void main() {
            gl_Position = aPosition;
            vTexCoord = aTexCoord;
        }
    """.trimIndent()
    
    // Fragment shader with multiple effects
    private val fragmentShaderCode = """
        precision mediump float;
        
        uniform sampler2D uTexture;
        uniform int uEffect;
        varying vec2 vTexCoord;
        
        // Grayscale conversion
        vec3 toGrayscale(vec3 color) {
            float gray = dot(color, vec3(0.299, 0.587, 0.114));
            return vec3(gray);
        }
        
        // Invert colors
        vec3 invertColor(vec3 color) {
            return vec3(1.0 - color.r, 1.0 - color.g, 1.0 - color.b);
        }
        
        // Simple edge detection (Sobel-like)
        vec3 edgeDetection(sampler2D tex, vec2 coord) {
            float offset = 0.002;
            
            // Sample surrounding pixels
            float tl = texture2D(tex, coord + vec2(-offset, offset)).r;
            float t  = texture2D(tex, coord + vec2(0.0, offset)).r;
            float tr = texture2D(tex, coord + vec2(offset, offset)).r;
            float l  = texture2D(tex, coord + vec2(-offset, 0.0)).r;
            float r  = texture2D(tex, coord + vec2(offset, 0.0)).r;
            float bl = texture2D(tex, coord + vec2(-offset, -offset)).r;
            float b  = texture2D(tex, coord + vec2(0.0, -offset)).r;
            float br = texture2D(tex, coord + vec2(offset, -offset)).r;
            
            // Sobel operator
            float gx = -tl - 2.0*l - bl + tr + 2.0*r + br;
            float gy = -tl - 2.0*t - tr + bl + 2.0*b + br;
            float edge = sqrt(gx*gx + gy*gy);
            
            return vec3(edge);
        }
        
        void main() {
            vec4 texColor = texture2D(uTexture, vTexCoord);
            vec3 color = texColor.rgb;
            
            if (uEffect == 1) {
                // Grayscale
                color = toGrayscale(color);
            } else if (uEffect == 2) {
                // Invert
                color = invertColor(color);
            } else if (uEffect == 3) {
                // Edge detection
                color = edgeDetection(uTexture, vTexCoord);
            }
            
            gl_FragColor = vec4(color, texColor.a);
        }
    """.trimIndent()
    
    // Vertex coordinates (full screen quad)
    private val vertexCoords = floatArrayOf(
        -1.0f,  1.0f,  // Top left
        -1.0f, -1.0f,  // Bottom left
         1.0f,  1.0f,  // Top right
         1.0f, -1.0f   // Bottom right
    )
    
    // Texture coordinates
    private val textureCoords = floatArrayOf(
        0.0f, 0.0f,  // Bottom left
        0.0f, 1.0f,  // Top left
        1.0f, 0.0f,  // Bottom right
        1.0f, 1.0f   // Top right
    )
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        
        // Initialize buffers
        vertexBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexCoords)
        vertexBuffer?.position(0)
        
        textureBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureCoords)
        textureBuffer?.position(0)
        
        // Create shader program
        program = createProgram(vertexShaderCode, fragmentShaderCode)
        
        if (program == 0) {
            Log.e(TAG, "Failed to create shader program")
            return
        }
        
        // Get shader handles
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        samplerHandle = GLES20.glGetUniformLocation(program, "uTexture")
        effectHandle = GLES20.glGetUniformLocation(program, "uEffect")
        
        // Generate texture
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        
        Log.d(TAG, "OpenGL initialized successfully")
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }
    
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        
        GLES20.glUseProgram(program)
        
        // Set effect uniform
        val effectValue = when (currentEffect) {
            ShaderEffect.NORMAL -> 0
            ShaderEffect.GRAYSCALE -> 1
            ShaderEffect.INVERT -> 2
            ShaderEffect.EDGE_DETECTION -> 3
        }
        GLES20.glUniform1i(effectHandle, effectValue)
        
        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(samplerHandle, 0)
        
        // Set vertex positions
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        
        // Set texture coordinates
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
        
        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        
        // Cleanup
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }
    
    fun updateTexture(bitmap: android.graphics.Bitmap) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
    }
    
    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) return 0
        
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShader == 0) return 0
        
        var program = GLES20.glCreateProgram()
        if (program == 0) {
            Log.e(TAG, "Failed to create program")
            return 0
        }
        
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Failed to link program: ${GLES20.glGetProgramInfoLog(program)}")
            GLES20.glDeleteProgram(program)
            program = 0
        }
        
        return program
    }
    
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        
        if (compiled[0] == 0) {
            Log.e(TAG, "Failed to compile shader: ${GLES20.glGetShaderInfoLog(shader)}")
            GLES20.glDeleteShader(shader)
            return 0
        }
        
        return shader
    }
}
