package com.example.a3dmodelviewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Choreographer
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.a3dmodelviewer.ui.theme._3dmodelViewerTheme
import com.google.android.filament.Skybox
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.KtxLoader
import com.google.android.filament.utils.Mat4
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import com.google.android.filament.utils.rotation
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {
    companion object {
        init { Utils.init() }
    }
    private lateinit var surfaceView: SurfaceView
    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: ModelViewer

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        surfaceView = SurfaceView(this).apply { setContentView(this) }
        choreographer = Choreographer.getInstance()
        modelViewer = ModelViewer(surfaceView)
        surfaceView.setOnTouchListener(modelViewer)
        enableEdgeToEdge()
//        setContent {
//            _3dmodelViewerTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
            loadGlb("porsche")
            loadGlb("DamagedHelmet")
            loadEnvironment("venetian_crossroads_2k")
            modelViewer.scene.skybox = Skybox.Builder().build(modelViewer.engine)

    }

    private fun loadEnvironment(ibl: String) {
        // Create the indirect light source and add it to the scene.
        var buffer = readAsset("envs/$ibl/${ibl}_ibl.ktx")
        KtxLoader.createIndirectLight(modelViewer.engine, buffer).apply {
            intensity = 100000f
            modelViewer.scene.indirectLight = this
        }

        // Create the sky box and add it to the scene.
//        buffer = readAsset("envs/$ibl/${ibl}_skybox.ktx")
//        KtxLoader.createSkybox(modelViewer.engine, buffer).apply {
//            modelViewer.scene.skybox = this
//        }
    }
    private val frameCallback = object : Choreographer.FrameCallback {
        private val startTime = System.nanoTime()
        override fun doFrame(currentTime: Long) {
            val seconds = (currentTime - startTime).toDouble() / 1_000_000_000
            choreographer.postFrameCallback(this)
            modelViewer.animator?.apply {
                if (animationCount > 0) {
                    applyAnimation(0, seconds.toFloat())
                }
                updateBoneMatrices()
            }
            modelViewer.render(currentTime)
//            modelViewer.asset?.apply {
//                modelViewer.transformToUnitCube()
//                val rootTransform = this.root.getTransform()
//                val degrees = 20f * seconds.toFloat()
//                val zAxis = Float3(0f, 0f, 1f)
//                this.root.setTransform(rootTransform * rotation(zAxis, degrees))
//            }
//            modelViewer.render(currentTime)
        }
    }
    private fun Int.getTransform(): Mat4 {
        val tm = modelViewer.engine.transformManager
        return Mat4.of(*tm.getTransform(tm.getInstance(this), null))
    }

    private fun Int.setTransform(mat: Mat4) {
        val tm = modelViewer.engine.transformManager
        tm.setTransform(tm.getInstance(this), mat.toFloatArray())
    }

    private fun loadGltf(name: String) {
        val buffer = readAsset("models/${name}.gltf")
        modelViewer.loadModelGltf(buffer) { uri -> readAsset("models/$uri") }
        modelViewer.transformToUnitCube()
    }

    private fun loadGlb(name: String) {
        val buffer = readAsset("models/${name}.glb")
        modelViewer.loadModelGlb(buffer)
        modelViewer.transformToUnitCube()
    }

    private fun readAsset(assetName: String): ByteBuffer {
        val input = assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }
//    private val frameCallback = object : Choreographer.FrameCallback {
//        override fun doFrame(currentTime: Long) {
//            choreographer.postFrameCallback(this)
//            modelViewer.render(currentTime)
//        }
//    }

    override fun onResume() {
        super.onResume()
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        choreographer.removeFrameCallback(frameCallback)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    _3dmodelViewerTheme {
        Greeting("Android")
    }
}