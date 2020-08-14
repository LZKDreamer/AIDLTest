package com.flyaudio.aidltest

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log
import com.flyaudio.aidltest.aidl.IAidlListener
import com.flyaudio.aidltest.aidl.IMyAidlInterface
import com.flyaudio.aidltest.aidl.Person
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.random.Random

/**
 * Author: LiaoZhongKai
 * Date: 2020/8/14 10:13
 * Description:
 */
class MyAidlService : Service() {

    //类型必须是ArrayList
    private var mPersons: ArrayList<Person> = ArrayList()

    private val mBinder: IBinder =
        MyAidlBinder()

    private val mRemoteCallbackList: RemoteCallbackList<IAidlListener> = RemoteCallbackList()

    override fun onBind(intent: Intent?): IBinder? {
        mPersons = ArrayList()
        return mBinder
    }

    inner class MyAidlBinder : IMyAidlInterface.Stub(){
        override fun registerListener(listener: IAidlListener) {
            mRemoteCallbackList.register(listener)
        }

        override fun unregisterListener(listener: IAidlListener) {
            mRemoteCallbackList.unregister(listener)
        }

        override fun addPerson(person: Person): MutableList<Person> {
            mPersons.add(person)
            return mPersons
        }

        override fun add(a: Int, b: Int): Int {
            Log.d("TAG","收到了远程的请求：输入的参数是:a=${a},b=${b}")
            return a+b
        }

        override fun requestArticleInfo() {
            HttpRepository().requestGirlList(1).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                Log.d("TAG","网络请求信息:${it}")
                val count = mRemoteCallbackList.beginBroadcast()
                val random = java.util.Random()
                val index = random.nextInt(it.data.size-1)
                for (i in 0 until count){
                    try {
                        mRemoteCallbackList.getBroadcastItem(i).getArtcileInfo(it.data[index].desc)
                    }catch (e: RemoteException){
                        Log.d("TAG","网络请求报错:${e.toString()}")
                    }
                }
                mRemoteCallbackList.finishBroadcast()
            }
        }

    }
}