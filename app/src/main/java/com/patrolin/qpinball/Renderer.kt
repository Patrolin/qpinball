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
            
            uniform lowp vec2 uResolution;
            in vec2 vPos;
            in float vRadius;
            
            out vec2 fPos;
            out float fRadius;
            out vec4 fColor;
            
            void main() {
                gl_PointSize = vRadius * 2.0;
                gl_Position = vec4(vPos.x, vPos.y, 0.0, 1.0);
                fPos = vec2(vPos.x * uResolution.x/2.0, vPos.y * uResolution.y/2.0);
                fRadius = vRadius;
                fColor = vec4(1.0, 0.0, 0.0, 1.0);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            
            uniform lowp vec2 uResolution;
            in vec2 fPos;
            in float fRadius;
            in vec4 fColor;
            
            out vec4 color;
            
            float linStep(float x, float a, float b) {
                return clamp((x-a)/b, 0.0, 1.0);
            }
            float L2(vec2 v) {
                return sqrt(v.x*v.x + v.y*v.y);
            }
            void drawCircleFlat() {
                vec2 pixelPos = vec2(gl_FragCoord.x - uResolution.x/2.0, gl_FragCoord.y - uResolution.y/2.0);
                float r = L2(pixelPos - fPos);
                color = fColor * vec4(r < fRadius);
            }
            void drawCircle() {
                vec2 pixelPos = vec2(gl_FragCoord.x - uResolution.x/2.0, gl_FragCoord.y - uResolution.y/2.0);
                float r = L2(pixelPos - fPos);
                float circleMask = 1.0 - linStep(r, fRadius, 1.0);
                color = vec4(fColor.rgb, fColor.a * circleMask);
            }
            void main() {
                drawCircle();
            }
        """.trimIndent())
        val (newBufferId, newBufferElementSize) = glSetupBuffer(programId, listOf("vPos", "vRadius"))
        bufferId = newBufferId
        bufferElementSize = newBufferElementSize
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1f)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glEnable(GLES30.GL_BLEND)
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
            bufferOffset += glWriteFloat(bufferData, ball.radius.toFloat())
        }
        if (bufferOffset % bufferElementSize != 0) fail("Written incorrect element size")
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, bufferId)
        //debugPrint("bufferId: $bufferId, ${bufferData.array().map{ it.toUInt().toString(2) }}")
        bufferData.rewind()
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, bufferData.limit(), bufferData, GLES30.GL_STATIC_DRAW)
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, balls.size)
    }
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        debugPrint("windowSize: $width, $height")
        GLES30.glViewport(0,0, width, height)
        GLES30.glUniform2f(GLES30.glGetUniformLocation(programId, "uResolution"), width.toFloat(), height.toFloat())
    }
}