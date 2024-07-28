package com.taehokimmm.hapticvboard_android.manager

import android.content.Context
import android.hardware.usb.UsbManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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