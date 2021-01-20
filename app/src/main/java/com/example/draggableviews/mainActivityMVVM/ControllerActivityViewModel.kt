package com.example.draggableviews.mainActivityMVVM

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.draggableviews.utils.IPAddress
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.*
import javax.inject.Inject

class ControllerActivityViewModel @Inject constructor(private val repository: Repository) : ViewModel(){

    private val imageSize = 5273

    private var job: Job? = null
    private var mSocket: Socket? = null

    private var data = byteArrayOf(255.toByte(), 127, 127, 127, 127, 1)

    private var version = MutableLiveData<String>()
    private var images = MutableLiveData<ByteArray>()
    var isConnected = MutableLiveData<Boolean>()

    fun getVersion(): LiveData<String> {
        repository.getVersion().enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>, response: Response<String>) {
                version.value = response.body()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                version.value = "Error"
            }
        })
        return version
    }

    fun checkingConnection() = isConnected

    fun start(): LiveData<ByteArray> {

        if(job != null && job!!.isActive){
            job!!.cancel()
            isConnected.value = false
            return images
        }

        job = CoroutineScope(Dispatchers.IO).launch {
            mSocket = Socket(IPAddress.ip, IPAddress.serverPort)
//            Thread.sleep(50)
            val datagramSocket = DatagramSocket()
            datagramSocket.soTimeout = 500
            val address: InetAddress = InetAddress.getByName(IPAddress.ip)
            while (true) {
                if(job != null && job!!.isActive) {
                    GlobalScope.launch(Dispatchers.Main) {
                        isConnected.value = true
                    }
                }
                else{
                    GlobalScope.launch(Dispatchers.Main) {
                        isConnected.value = false
                    }
                }

                data[1] = horizontalValueFirst.toByte()
                data[2] = verticalValueFirst.toByte()
                data[3] = horizontalValueSecond.toByte()
                data[4] = verticalValueSecond.toByte()
                data[5] = mode.toByte()
                val outputPack = DatagramPacket(data, data.size, address, IPAddress.datagramPort)
                datagramSocket.send(outputPack)

                val inputPack = ByteArray(imageSize)
                val inputData = DatagramPacket(inputPack, inputPack.size, address, mSocket!!.port)
                try {
                    datagramSocket.receive(inputData)
                    val byteArray = inputData.data
                    println(byteArray)
                    GlobalScope.launch(Dispatchers.Main) {
                        images.value = byteArray
                    }
                } catch (e: SocketTimeoutException) {
                    Log.d("tag", "time error")
                    continue
                }
                delay(1000 / 30)
            }
        }
        return images
    }
}