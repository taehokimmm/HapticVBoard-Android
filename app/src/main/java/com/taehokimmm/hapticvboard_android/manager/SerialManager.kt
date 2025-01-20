package com.taehokimmm.hapticvboard_android.manager

import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.hoho.android.usbserial.driver.SerialTimeoutException
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.hoho.android.usbserial.util.SerialInputOutputManager.Listener
import com.taehokimmm.hapticvboard_android.layout.vibrationtest.delay
import java.io.IOException

class SerialManager(context: Context) {
    private var manager: UsbManager? = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var port: UsbSerialPort? = null
    private val WAIT = 2000
    private lateinit var writeHandler: Handler
    private lateinit var readHandler: Handler
    private var usbIOManager: SerialInputOutputManager? = null
    private var response: String = ""
    private var isDone: Boolean = false

    init {
        connect()
//        val writeThread = HandlerThread("SerialWriteThread")
//        writeThread.start()
//        writeHandler = Handler(writeThread.looper)

//        val readThread = HandlerThread("SerialReadThread")
//        readThread.start()
//        readHandler = Handler(readThread.looper)
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

                    val readListener = object : Listener {
                        override fun onNewData(data: ByteArray) {
                            val data_string = String(data)
                            if (data_string == "\n") {
                                Log.d("SerialComm", "Read:  ${response}")
                                if (response.trim().endsWith("done")) isDone = true
                                response = ""
                            } else {
                                response += data_string
                            }
                        }

                        override fun onRunError(e: java.lang.Exception?) {
                            Log.d("SerialComm", "onRunError")
                        }
                    }

                    usbIOManager = SerialInputOutputManager(port, readListener);
                    usbIOManager!!.start()
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
        if (port == null) {
            throw IOException("Port is not open")
        }
        try {
            if (!isDone) {
                port?.write("q\n".toByteArray(), 1)
                Log.d("SerialComm", "Written: QUIT")
                delay({
                    port?.write(data, 1)
                    isDone = false
                    Log.d("SerialComm", "Written: ${String(data)}")
                      }, 5)
            } else {
                port?.write(data,  1)
                isDone = false
                Log.d("SerialComm", "Written: ${String(data)}")
            }
        } catch (e: SerialTimeoutException) {
            throw IOException("Write timeout occurred.", e)
        } catch (e: IOException) {
            throw IOException("Error occurred during writing.", e)
        }
//        writeHandler.post {
//            try {
//                port?.write(data, 0)
//                Log.d("SerialWrite", "Data written: ${String(data)}")
//                //read()
//            } catch (e: IOException) {
//                Log.e("SerialError", "Error writing to serial port", e)
//            }
//        }
    }

    fun isOpen(): Boolean {
        return port != null
    }

    fun read(timeout: Int = 10) {
//        readHandler.post {
//            try {
//                var data: ByteArray = ByteArray(64);
//                port?.read(data, timeout)
//                var data_string = String(data)
//                Log.d("SerialRead", "Data Read : ${data_string}")
//                if (data_string.contains("done")) {
//                    Log.d("SerialRead", "Play DONE!");
//                }
//            } catch (e: IOException) {
//                Log.e("SerialError", "Error writing to serial port", e)
//            }
//        }
    }

    protected fun finalize() {
        close()
    }
}