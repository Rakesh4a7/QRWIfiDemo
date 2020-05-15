package com.rakesh.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import com.thanosfisherman.wifiutils.wifiDisconnect.DisconnectionErrorCode
import com.thanosfisherman.wifiutils.wifiDisconnect.DisconnectionSuccessListener
import kotlinx.android.synthetic.main.activity_wifi.*
import java.io.*
import java.net.Socket
import java.net.UnknownHostException


class ScanWifi : AppCompatActivity() {

    private var context: Context = this

    companion object {
        private const val PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 120
    }

    private lateinit var USERNAME: String
    private lateinit var PASSWORD: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi)

        WifiUtils.enableLog(true)

        buttonConnect.setOnClickListener {

            USERNAME = editTextEmail.text.toString().trim()
            PASSWORD = editTextPassword.text.toString().trim()

            if (USERNAME.isEmpty()) {
                editTextEmail.error = "UserName required"
                editTextEmail.requestFocus()
                return@setOnClickListener
            }

            if (PASSWORD.isEmpty()) {
                editTextPassword.error = "Password required"
                editTextPassword.requestFocus()
                return@setOnClickListener
            }
            if (checkPermissions()) {
                WifiUtils.withContext(context).enableWifi(this::checkResult);
            }
        }

        buttonSend.setOnClickListener{
            if(hasInternet())  runTcpClient()
        }
    }

    private fun checkResult(isSuccess: Boolean) {
        if (isSuccess) {
            Toast.makeText(context, "WIFI ENABLED", Toast.LENGTH_SHORT).show()
            WifiUtils.withContext(applicationContext).scanWifi(this::getScanResults).start()
        } else
            Toast.makeText(context, "COULDN'T ENABLE WIFI", Toast.LENGTH_SHORT).show()
    }

    private fun getScanResults(results: List<ScanResult>) {
        if (results.isEmpty()) {
            Log.i(localClassName, "SCAN RESULTS IT'S EMPTY")
            return
        }
        Log.i(localClassName, "GOT SCAN RESULTS $results")
        for (ScanResult in results) {
            if (ScanResult.SSID == USERNAME) {
                connectWithWpa(context)
                break
            }
        }
    }

    private fun connectWithWpa(context: Context) {
        WifiUtils.withContext(context)
            .connectWith(USERNAME, PASSWORD)
            .setTimeout(15000)
            .onConnectionResult(object : ConnectionSuccessListener {
                override fun success() {
                    Toast.makeText(context, "SUCCESS!", Toast.LENGTH_SHORT).show()
                }

                override fun failed(errorCode: ConnectionErrorCode) {
                    Toast.makeText(context, "EPIC FAIL!$errorCode", Toast.LENGTH_SHORT).show()
                }
            })
            .start()
    }

    private fun disconnect(context: Context) {
        WifiUtils.withContext(context)
            .disconnectFrom(USERNAME, object : DisconnectionSuccessListener {
                override fun success() {
                    Toast.makeText(context, "Disconnect success!", Toast.LENGTH_SHORT).show()
                }

                override fun failed(errorCode: DisconnectionErrorCode) {
                    Toast.makeText(context, "Failed to disconnect: $errorCode", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION
            )
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION -> {
                connectWithWpa(context)
            }
        }
    }

    private fun runTcpClient() {
        val thread: Thread = object : Thread() {
            override fun run() {
                super.run()
                try {
                    var ipAddress = editIp.text.toString().trim()
                    var port = editPort.text.toString().toInt()
                    var message = editMessage.text.toString().trim()

                    val s = Socket(ipAddress, port)
                    s.soTimeout = 10000
                    val `in` =
                        BufferedReader(InputStreamReader(s.getInputStream()))
                    val out =
                        BufferedWriter(OutputStreamWriter(s.getOutputStream()))
                    Log.i("TcpClient", "Connected!")
                    //send output msg
                    val outMsg: String = message + "\n"
                    out.write(outMsg)
                    out.flush()
                    Log.i("TcpClient", "sent: $outMsg")
                    //accept server response
                    val inMsg =
                        `in`.readLine() + System.getProperty("line.separator")
                    Log.i("TcpClient", "received: $inMsg")
                    //close connection
                    s.close()
                } catch (e: UnknownHostException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        thread.start()
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