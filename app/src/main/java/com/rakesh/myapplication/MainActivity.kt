package com.rakesh.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.rakesh.myapplication.api.RetrofitClient
import com.rakesh.myapplication.api.TcpClient
import com.rakesh.myapplication.model.DefaultResponse
import retrofit2.Callback
import retrofit2.Response
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private var context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.scan).setOnClickListener {
            // startActivity(Intent(this,ScanQrAct::class.java))
            letsScan()
        }
    }

    private fun letsScan() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("scan")
        integrator.setCameraId(0)
        integrator.setOrientationLocked(true)
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        showDialog("QR Code scanned")
        scanResult.let {
            var QRvalue = scanResult.contents
         //   sendQRData(QRvalue)
            send(QRvalue)
        }
    }

    private fun sendQRData(QRData: String) {
        RetrofitClient.instance.sendData(QRData).enqueue(object : Callback<DefaultResponse> {
            override fun onFailure(call: retrofit2.Call<DefaultResponse>, t: Throwable) {
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: retrofit2.Call<DefaultResponse>,
                response: Response<DefaultResponse>
            ) {
                Toast.makeText(applicationContext, response.body()?.message, Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun showDialog(message: String) {
        val dialogBuilder = AlertDialog.Builder(context!!)
        dialogBuilder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(this, ScanWifi::class.java))
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Info")
        alert.show()
    }

    private fun send(qrCode : String) {
        TcpClient.connect(context, "192.168.4.1", 4096, qrCode)
      /*  val client = Socket("192.168.4.1", 4096)
        client.outputStream.write(qrCode.toByteArray())
        client.close()*/
    }

}

