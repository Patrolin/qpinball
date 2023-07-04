package com.patrolin.qpinball

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.patrolin.qpinball.common.*
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class Renderer : GLSurfaceView.Renderer {
    var programId: Int = 0
    val attribNames = listOf("vPos")
    val bufferIds = IntBuffer.allocate(attribNames.size)
    lateinit var vPosBuffer: FloatBuffer
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        programId = createGLProgram("""
            #version 300 es
            layout(location = 0) in vec2 vPos;
            out vec4 fColor;
            void main() {
                gl_PointSize = 10.0;
                gl_Position = vec4(vPos.x, vPos.y, 0.0, 1.0);
                fColor = vec4(1.0, 0.0, 0.0, 1.0);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            
            in vec4 fColor;
            out vec4 color;
            void main(){
                color = fColor;
            }
        """.trimIndent())
        GLES30.glGenBuffers(bufferIds.limit(), bufferIds)
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1f)
    }
    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        simulate()
        // send data
        val vPosBufferId = bufferIds[0]
        vPosBuffer = FloatBuffer.allocate(balls.size * 2)
        for (i in balls.indices) {
            val ball = balls[i]
            vPosBuffer.put(ball.x.toFloat())
            vPosBuffer.put(ball.y.toFloat())
        }
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vPosBufferId)
        vPosBuffer.rewind()
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vPosBuffer.limit()*4, vPosBuffer, GLES30.GL_STATIC_DRAW)
        // specify layout
        val vPosId = 0
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vPosBufferId)
        GLES30.glEnableVertexAttribArray(vPosId)
        GLES30.glVertexAttribPointer(vPosId, 2, GLES30.GL_FLOAT, false, 0, 0)
        // draw
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, balls.size)
    }
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0,0, width, height)
    }
}
fun createGLProgram(vertexShader: String, fragmentShader: String): Int {
    val vertexShaderId = createShader(vertexShader, GLES30.GL_VERTEX_SHADER)
    val fragmentShaderId = createShader(fragmentShader, GLES30.GL_FRAGMENT_SHADER)
    val programId = GLES30.glCreateProgram()
    if (programId == 0) throw Exception("Failed to create GL program")
    GLES30.glAttachShader(programId, vertexShaderId)
    GLES30.glAttachShader(programId, fragmentShaderId)
    // link
    GLES30.glLinkProgram(programId)
    val linkStatus = IntArray(1)
    GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
    if (linkStatus[0] == 0) throw Exception("Failed to link GL program")
    GLES30.glUseProgram(programId)
    return programId
}
fun createShader(src: String, type: Int): Int {
    val shaderId = GLES30.glCreateShader(type)
    if (shaderId == 0) throw Exception("Failed to create shader")
    GLES30.glShaderSource(shaderId, src)
    GLES30.glCompileShader(shaderId)
    val compileStatus = IntArray(1)
    GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
    val shaderLog = GLES30.glGetShaderInfoLog(shaderId)
    if ((compileStatus[0] == 0) || shaderLog.startsWith("ERROR"))
        throw Exception("Failed to compile shader (compileStatus=${compileStatus[0]}):\n\n${src}\n\n${shaderLog}")
    return shaderId
}