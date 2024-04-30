package com.mobile.narciso

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mobile.narciso.databinding.ActivityCameraBinding
import java.util.Arrays

class CameraActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    private lateinit var binding: ActivityCameraBinding
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Assegna questa attività come listener alla TextureView
        binding.textureView.surfaceTextureListener = this



        // Controlla i permessi della fotocamera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Richiedi i permessi se non sono stati concessi
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            initializeCamera();

        }
    }

    // Implementazione dei metodi dell'interfaccia TextureView.SurfaceTextureListener

    /*fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        // SurfaceTexture è disponibile, puoi inizializzare la fotocamera qui
        initializeCamera()
    }*/

    /*fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        // Le dimensioni della TextureView sono cambiate, gestisci l'aggiornamento se necessario
    }*/

    /*fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        // SurfaceTexture è stato distrutto, rilascia le risorse della fotocamera se necessario
        return true
    }*/

    /*fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        // La TextureView è stata aggiornata, gestisci l'aggiornamento se necessario
    }*/
    // Altri metodi della classe...

    private fun initializeCamera() {
        try {
            val textureView = binding.textureView
            val surfaceTexture = textureView.surfaceTexture ?: return

            // Imposta le dimensioni predefinite del buffer sulla larghezza e sull'altezza della TextureView
            surfaceTexture.setDefaultBufferSize(textureView.width, textureView.height)

            val surface = Surface(surfaceTexture)

            // Ottieni un riferimento al servizio della fotocamera
            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            Toast.makeText(this, "debug 3", Toast.LENGTH_SHORT).show()
            // Ottenere l'ID della fotocamera posteriore
            var cameraId: String? = null
            for (id in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id
                    break
                }
            }

            // Gestione dell'apertura della fotocamera
            if (cameraId != null) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        val captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                        captureRequestBuilder.addTarget(surface)

                        camera.createCaptureSession(Arrays.asList(surface),
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigured(session: CameraCaptureSession) {
                                    // La configurazione della sessione di cattura è stata completata
                                }

                                override fun onConfigureFailed(session: CameraCaptureSession) {
                                    // La configurazione della sessione di cattura è fallita
                                    Toast.makeText(this@CameraActivity, "Configurazione della sessione di cattura fallita.", Toast.LENGTH_SHORT).show()
                                }
                            }, null)
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        // La fotocamera è stata disconnessa
                        camera.close()
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        // Si è verificato un errore con la fotocamera
                        camera.close()
                    }
                }, null)
            } else {
                // Nessuna fotocamera posteriore trovata
                Toast.makeText(this, "Nessuna fotocamera posteriore disponibile.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Gestione delle eccezioni
            e.printStackTrace()
            Toast.makeText(this, "Si è verificato un errore: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
        TODO("Not yet implemented")
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
        TODO("Not yet implemented")
    }
}
