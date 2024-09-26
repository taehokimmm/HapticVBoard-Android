package com.taehokimmm.hapticvboard_android.manager

import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.hoho.android.usbserial.driver.SerialTimeoutException
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.io.IOException

class SerialManager(context: Context) {
    private var manager: UsbManager? = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var port: UsbSerialPort? = null
    private val WAIT = 2000
    private lateinit var writeHandler: Handler
    init {
        connect()
        val writeThread = HandlerThread("SerialWriteThread")
        writeThread.start()
        writeHandler = Handler(writeThread.looper)
    }

    fun connect() {
        try {
            val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
            if (availableDrivers.isNotEmpty()) {
                val driver = availableDrivers[0]
                val connection = manager?.openDevice(driver.device)
                if (connection != null) {
                    port = driver.ports[0]
                    port?.open(connection)
                    port?.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun close() {
        try {
            port?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun write(data: ByteArray, timeout: Int = WAIT) {
//        if (port == null) {
//            throw IOException("Port is not open")
//        }
//
//        try {
//            port?.write(data,  1)
//        } catch (e: SerialTimeoutException) {
//            throw IOException("Write timeout occurred.", e)
//        } catch (e: IOException) {
//            throw IOException("Error occurred during writing.", e)
//        }
        writeHandler.post {
            try {
                port?.write(data, 10)
                Log.d("SerialWrite", "Data written: ${String(data)}")
                //read()
            } catch (e: IOException) {
                Log.e("SerialError", "Error writing to serial port", e)
            }
        }
    }

    fun isOpen(): Boolean {
        return port != null
    }

    fun read(timeout: Int = WAIT): Int {
        if (port == null) {
            throw IOException("Port is not open")
        }

        return try {
            var data: ByteArray = ByteArray(10000);
            port?.read(data, timeout) ?: throw IOException("Failed to read data")
            Log.d("SerialRead", "Data Read : ${String(data)}")
        } catch (e: IOException) {
            throw IOException("Error occurred during reading.", e)
        }
    }

    protected fun finalize() {
        close()
    }
}