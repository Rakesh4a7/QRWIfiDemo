package com.rakesh.myapplication.api

import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import java.net.UnknownHostException

object TcpClient {
    private var socket: Socket? = null
    private var out: PrintWriter? = null
    private var connected = false
    private var message : String = ""
    private val TAG = TcpClient::class.java.simpleName

    fun connect(context: Context, host: String?, port: Int, message: String) {
        this.message = message
        ConnectTask(context).execute(host, port.toString())
    }

    class ConnectTask(private val context: Context) :
        AsyncTask<String?, Void?, Void?>() {

        private val host: String? = null
        private val port = 0

        override fun onPreExecute() {
            showToast(context, "Connecting..")
            super.onPreExecute()
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            if (connected) {
                showToast(context, "Connection successfull")
                send(message)
            }
        }

        override fun doInBackground(vararg params: String?): Void? {
            try {
                val host = params[0]
                val port = params[1]!!.toInt()
                socket = Socket(host, port)
                out = PrintWriter(socket!!.getOutputStream(), true)
            } catch (e: UnknownHostException) {
                showToast(context, "Don't know about host: $host:$port")
            } catch (e: IOException) {
                showToast(context, "Couldn't get I/O for the connection to: $host:$port")
            }
            connected = true
            return null
        }

    }

    fun disconnect(context: Context) {
        if (connected) {
            try {
                out!!.close()
                socket!!.close()
                connected = false
            } catch (e: IOException) {
                showToast(context, "Couldn't get I/O for the connection")
            }
        }
    }

    /**
     * Send command to a Pure Data audio engine.
     */
    fun send(command: String) {
        if (connected) out!!.println("$command;")
    }

    private fun showToast(context: Context, message: String) {
        Handler(context.mainLooper).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

}