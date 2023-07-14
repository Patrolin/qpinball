package com.patrolin.qpinball

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.patrolin.qpinball.common.*
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Renderer : GLSurfaceView.Renderer {
    var programId = 0
    var bufferId = 0
    var bufferElementSize = 0
    var bufferData = ByteBuffer.allocate(0)
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        programId = glCreateProgram("""
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
        val (newBufferId, newBufferElementSize) = glSetupBuffer(programId)
        bufferId = newBufferId
        bufferElementSize = newBufferElementSize
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1f)
    }
    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        simulate()
        bufferData = ByteBuffer.allocate(bufferElementSize * balls.size)
        var bufferOffset = 0
        for (i in balls.indices) {
            val ball = balls[i]
            bufferOffset += glWriteFloat(bufferData, ball.x.toFloat())
            bufferOffset += glWriteFloat(bufferData, ball.y.toFloat())
        }
        if (bufferOffset % bufferElementSize != 0) fail("Written incorrect element size")
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, bufferId)
        //debugPrint("bufferId: $bufferId, ${bufferData.array().map{ it.toUInt().toString(2) }}")
        bufferData.rewind()
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, bufferData.limit(), bufferData, GLES30.GL_STATIC_DRAW)
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, balls.size)
    }
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0,0, width, height)
    }
}