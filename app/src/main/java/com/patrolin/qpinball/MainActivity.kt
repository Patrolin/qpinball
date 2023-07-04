package com.patrolin.qpinball

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var mGLSurfaceView: GLSurfaceView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGLSurfaceView = MyGLSurfaceView(this)
        setContentView(mGLSurfaceView)
    }
    class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
        private val mGLRenderer: Renderer
        init {
            setEGLContextClientVersion(3)
            mGLRenderer = Renderer()
            setRenderer(mGLRenderer)
        }
    }
    override fun onResume() {
        super.onResume()
        mGLSurfaceView.onResume()
    }
    override fun onPause() {
        super.onPause()
        mGLSurfaceView.onPause()
    }
}