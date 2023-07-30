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
            in int vShaderId;
            
            out vec2 fPos;
            out float fRadius;
            flat out int fShaderId;
            out vec4 fColor;
            
            void main() {
                gl_PointSize = vRadius * 2.0;
                gl_Position = vec4(vPos.x, vPos.y, 0.0, 1.0);
                fPos = vec2(vPos.x * uResolution.x/2.0, vPos.y * uResolution.y/2.0);
                fRadius = vRadius;
                fShaderId = vShaderId;
                fColor = vec4(1.0, 0.0, 0.0, 1.0);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            
            uniform lowp vec2 uResolution;
            in vec2 fPos;
            in float fRadius;
            flat in int fShaderId;
            in vec4 fColor;
            
            out vec4 color;
            
            float linStep(float x, float a, float b) {
                return clamp((x-a)/b, 0.0, 1.0);
            }
            float solveLerp(float a, float b, float c) {
                return (a - c) / (a - b);
            }
            float L2(vec2 v) {
                return sqrt(v.x*v.x + v.y*v.y);
            }
            vec2 normalizeIfBigger(vec2 v, float R) {
                float L = L2(v);
                if (L > R) return v * vec2(R/L);
                return v;
            }
            float gaOuterProduct(vec2 a, vec2 b) {
                return a.x*b.y - a.y*b.x;
            }
            
            void drawCircleFlat() {
                vec2 pixelPos = vec2(gl_FragCoord.x - uResolution.x/2.0, gl_FragCoord.y - uResolution.y/2.0);
                float r = L2(pixelPos - fPos);
                float circleMask = float(r < fRadius);
                color = vec4(fColor.rgb, fColor.a * circleMask);
            }
            void drawCircleSDF() {
                vec2 pixelPos = vec2(gl_FragCoord.x - uResolution.x/2.0, gl_FragCoord.y - uResolution.y/2.0);
                float r = L2(pixelPos - fPos);
                float circleMask = 1.0 - linStep(r, fRadius-1.1, 1.1);
                color = vec4(fColor.rgb, fColor.a * circleMask);
            }
            void drawCircleBetter() {
                vec2 centeredPixelPos = vec2(gl_FragCoord.x - uResolution.x/2.0, gl_FragCoord.y - uResolution.y/2.0) - fPos;
                vec2 A = vec2(centeredPixelPos.x - 0.5, centeredPixelPos.y - 0.5);
                A = normalizeIfBigger(A, fRadius);
                vec2 B = vec2(centeredPixelPos.x + 0.5, centeredPixelPos.y - 0.5);
                B = normalizeIfBigger(B, fRadius);
                vec2 C = vec2(centeredPixelPos.x + 0.5, centeredPixelPos.y + 0.5);
                C = normalizeIfBigger(C, fRadius);
                vec2 D = vec2(centeredPixelPos.x - 0.5, centeredPixelPos.y + 0.5);
                D = normalizeIfBigger(D, fRadius);
                float polygonArea = (gaOuterProduct(A, B) + gaOuterProduct(B, C)
                    + gaOuterProduct(C, D) + gaOuterProduct(D, A)) / 2.0;
                float circleMask = abs(polygonArea);
                color = vec4(fColor.rgb, fColor.a * circleMask);
            }
            void drawCircleBetterer() {
                vec2 centeredPixelPos = vec2(gl_FragCoord.x - uResolution.x/2.0, gl_FragCoord.y - uResolution.y/2.0) - fPos;
                vec2 A = vec2(centeredPixelPos.x - 0.5, centeredPixelPos.y - 0.5);
                vec2 B = vec2(centeredPixelPos.x + 0.5, centeredPixelPos.y - 0.5);
                vec2 C = vec2(centeredPixelPos.x + 0.5, centeredPixelPos.y + 0.5);
                vec2 D = vec2(centeredPixelPos.x - 0.5, centeredPixelPos.y + 0.5);
                vec2 P_A = vec2(clamp(-sqrt(fRadius*fRadius - A.y*A.y), A.x, B.x), A.y);
                vec2 P_B = vec2(B.x, clamp(-sqrt(fRadius*fRadius - B.x*B.x), B.y, C.y));
                vec2 P_C = vec2(clamp(sqrt(fRadius*fRadius - C.y*C.y), D.x, C.x), C.y);
                vec2 P_D = vec2(D.x, clamp(sqrt(fRadius*fRadius - D.x*D.x), A.y, D.y));
                float polygonArea = (gaOuterProduct(P_A, P_B) + gaOuterProduct(P_B, P_C)
                    + gaOuterProduct(P_C, P_D) + gaOuterProduct(P_D, P_A)) / 2.0;
                float circleMask = abs(polygonArea);
                color = vec4(fColor.rgb, fColor.a * circleMask);
            }
            void drawCircleGroundTruth() {
                vec2 pixelPos = vec2(gl_FragCoord.x - uResolution.x/2.0, gl_FragCoord.y - uResolution.y/2.0);
                float PHI_1 = (1.0 + sqrt(5.0)) / 2.0;
                float PHI_2 = 1.324717957244746025960908854;
                int accMask = 0;
                int N = 1000;
                for (int i = 0; i < N; i++) {
                    vec2 rand = vec2(
                        mod((float(i) / PHI_1), 1.0) - 0.5,
                        mod((float(i) / PHI_2), 1.0) - 0.5
                    );
                    float r = L2((pixelPos + rand) - fPos);
                    accMask += int(r < fRadius);
                }
                float circleMask = float(accMask) / float(N);
                color = vec4(fColor.rgb, fColor.a * circleMask);
            }
            void main() {
                if (fShaderId == 0) {
                    drawCircleFlat();
                } else if (fShaderId == 1) {
                    drawCircleSDF();
                } else if (fShaderId == 2) {
                    drawCircleBetter();
                } else if (fShaderId == 3) {
                    drawCircleGroundTruth();
                }
            }
        """.trimIndent())
        val (newBufferId, newBufferElementSize) = glSetupBuffer(programId, listOf("vPos", "vRadius", "vShaderId"))
        bufferId = newBufferId
        bufferElementSize = newBufferElementSize
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1f)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glEnable(GLES30.GL_BLEND)
    }
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        val projection = Vec2(1.0, 1.0).projection(Vec2(1.0, 0.0))
        val rejection = Vec2(1.0, 1.0).rejection(Vec2(1.0, 0.0))
        debugPrint("projection: $projection")
        debugPrint("rejection: $rejection")
        debugPrint("windowSize: $width, $height")
        GLES30.glViewport(0,0, width, height)
        GLES30.glUniform2f(GLES30.glGetUniformLocation(programId, "uResolution"), width.toFloat(), height.toFloat())
        screenSize = Vec2(width.toDouble(), height.toDouble())
    }
    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        simulate()
        bufferData = ByteBuffer.allocate(bufferElementSize * balls.size)
        var bufferOffset = 0
        for (i in balls.indices) {
            val ball = balls[i]
            bufferOffset += glWriteFloat(bufferData, ball.pos.x.toFloat())
            bufferOffset += glWriteFloat(bufferData, ball.pos.y.toFloat())
            bufferOffset += glWriteFloat(bufferData, ball.r.toFloat())
            bufferOffset += glWriteInt(bufferData, ball.shaderId)
        }
        if (bufferOffset % bufferElementSize != 0) fail("Written incorrect element size")
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, bufferId)
        //debugPrint("bufferId: $bufferId, ${bufferData.array().map{ it.toUInt().toString(2) }}")
        bufferData.rewind()
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, bufferData.limit(), bufferData, GLES30.GL_STATIC_DRAW)
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, balls.size)
    }
}