package com.rakesh.myapplication

import android.os.Handler
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.net.UnknownHostException

class SocketThread:Thread() {

    private lateinit var handler: Handler
    var writer : PrintWriter? = null

    override fun run() {
        super.run()
        try {
            val socket = Socket("192.168.1.181", 4096)
            val input = socket.getInputStream()
            val reader = BufferedReader(InputStreamReader(input))
            var text: String
            val output = socket.getOutputStream()
            writer = PrintWriter(output, true)
            while (true){
                text = reader.readLine()
                handler.sendMessage(handler.obtainMessage(0, text))
            }
        } catch (e: UnknownHostException) {
        } catch (e: IOException) {
        }

    }

    fun setHandler(handler: Handler) {
        this.handler = handler
    }

    fun sendMessage(string: String) {
        writer?.println(string)
    }

    override fun destroy() {

    }
}