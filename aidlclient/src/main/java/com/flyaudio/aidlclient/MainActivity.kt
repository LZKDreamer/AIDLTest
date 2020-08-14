package com.flyaudio.aidlclient

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Observable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.flyaudio.aidltest.aidl.IAidlListener
import com.flyaudio.aidltest.aidl.IMyAidlInterface
import com.flyaudio.aidltest.aidl.Person
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mIMyAidlInterface: IMyAidlInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //绑定服务
        bindService()

        //AIDL远程计算
        main_btn.setOnClickListener {
            val num1 = main_a_et.text.trim().toString().toInt()
            val num2 = main_b_et.text.trim().toString().toInt()
            try {
                //调用远程的服务
                val result = mIMyAidlInterface?.add(num1,num2)
                Log.d("TAG","result:${result}")
                main_result_tv.text = result?.toString()
            }catch (e: RemoteException){
                Log.e("TAG","远端服务调用报错：${e.toString()}")
                main_result_tv.text ="出错了"
            }
        }


        val random: Random = Random(20)
        main_param_btn.setOnClickListener {
            val person = Person("名字${random.nextInt()},",23)
            try {
                val list = mIMyAidlInterface?.addPerson(person)
                list?.let {
                    main_param_tv.text = it.toString()
                }
            }catch (e: RemoteException){
                Log.e("TAG","增加person报错:${e.toString()}")
            }
        }

        main_callback_btn.setOnClickListener {
            main_callback_btn.setText("加载中")
            main_callback_btn.isEnabled = false
            mIMyAidlInterface?.requestArticleInfo()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }

    private fun bindService() {
        val intent = Intent().apply {
            component = ComponentName(
                "com.flyaudio.aidltest",
                "com.flyaudio.aidltest.MyAidlService"
            )

        }
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private val mServiceConnection: ServiceConnection = object :ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {
            mIMyAidlInterface?.unregisterListener(mIAidlListener)
            mIMyAidlInterface = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("TAG","客户端服务连接成功")
            //拿到远程的服务
            mIMyAidlInterface = IMyAidlInterface.Stub.asInterface(service)
            mIMyAidlInterface!!.registerListener(mIAidlListener)
        }

    }

    private val mIAidlListener: IAidlListener = object : IAidlListener.Stub(){
        override fun getArtcileInfo(desc: String?) {
            //回调是在子线程中的
            runOnUiThread {
                main_callback_btn.setText("网络请求回调")
                main_callback_btn.isEnabled = true
                Log.d("TAG","客户端收到回调")
                if (desc != null){
                    main_callback_tv.text = desc
                }else{
                    main_callback_tv.text = "未获取到结果"
                }
            }
        }

    }
}
