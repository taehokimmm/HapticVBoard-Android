package com.taehokimmm.hapticvboard_android.manager

import android.content.Context
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.SerialTimeoutException
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.io.IOException

class SerialManager(context: Context) {
    private var manager: UsbManager? = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var port: UsbSerialPort? = null
    private val WAIT = 2000

    init {
        connect()
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
        if (port == null) {
            throw IOException("Port is not open")
        }

        try {
            port?.write(data, timeout)
        } catch (e: SerialTimeoutException) {
            throw IOException("Write timeout occurred.", e)
        } catch (e: IOException) {
            throw IOException("Error occurred during writing.", e)
        }
    }

    fun isOpen(): Boolean {
        return port != null
    }

    fun read(data: ByteArray, timeout: Int = WAIT): Int {
        if (port == null) {
            throw IOException("Port is not open")
        }

        return try {
            port?.read(data, timeout) ?: throw IOException("Failed to read data")
        } catch (e: IOException) {
            throw IOException("Error occurred during reading.", e)
        }
    }

    protected fun finalize() {
        close()
    }
}