package com.rakesh.myapplication

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
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
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private var context: Context = this
    private var qrCode: String = "wikileaks.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.scan).setOnClickListener {
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
            qrCode = scanResult.contents
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
                send()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Info")
        alert.show()
    }

    private fun showInternetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Info")
        builder.setMessage("No Internet Connection.\nEnable internet and Try Again.")

        builder.setPositiveButton("Try Again") { dialog, _ ->
            dialog.dismiss()
            send()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun send() {
        if (hasInternet())
            sendData()
        else {
            showInternetDialog()
        }
    }

    private fun sendData() {
      //  TcpClient.connect(context, "192.168.137.1", 4096, qrCode)  //192.168.4.1 //192.168.1.181

/*            try {
                Thread {
                val soc = Socket("192.168.137.1", 4096) //  192.168.1.181
                soc.soTimeout
                val dout = DataOutputStream(soc.getOutputStream())
                dout.writeUTF(qrCode)

                dout.flush()
                dout.close()
                soc.close()
                }.start()
            }catch (io: IOException){
                Log.e("Error", io.localizedMessage)
            } catch (e: Exception) {
                e.printStackTrace()
            }*/

        startActivity(Intent(this, ScanWifi::class.java))
    }

    private fun hasInternet(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                }
            }
        }
        return false
    }

}

