# AIDLTest
AIDL练习项目


## AIDL使用示例

#### 项目结构

![image-20200814162116608](C:\Users\user\Desktop\aidl1)

![image-20200814161924315](C:\Users\user\Desktop\aidl2)



#### AIDL支持的数据类型

- 基本数据类型(除short类型外)

- String

- Char

- List （集合中的元素必须是支持的类型或者是其它声明的AIDL对象）

  - 集合中的元素需要使用in、out、inout修饰；其中 in 表示数据只能由客户端流向服务端（即传入的参数）， out 表示数据只能由服务端流向客户端（即输出的参数），而 inout 则表示数据可在服务端与客户端之间双向流通

- Map （集合中的元素必须是支持的类型或者是其它声明的AIDL对象）

  - 集合中的元素同List

- Paraceble

- 示例

  ```java
  interface IMyAidlInterface {
  
      //计算两个数的和
      int add(int a,int b);//基本数据类型
  
      List<Person> addPerson(in Person person);//这里的参数是入参，所以用使用in修饰
  
      void registerListener(IAidlListener listener);
  
      void unregisterListener(IAidlListener listener);
  
      void requestArticleInfo();
  }
  ```

  



#### AIDL 类（的定义与序列化）

- 实现Parcelable接口

- 读和写的顺序要一致

  ```
  name = parcel.readString()!!,
  age = parcel.readInt()
  
  dest?.writeString(name)
  dest?.writeInt(age)
  ```

```kotlin
//实现Parcelable接口
data class Person(val name: String,val age: Int) : Parcelable {
    constructor(parcel: Parcel) : this(//<-----看这里
        //注意顺序
        name = parcel.readString()!!,
        age = parcel.readInt()
    ) {
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {//<-----看这里
        //注意顺序
        dest?.writeString(name)
        dest?.writeInt(age)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Person(name='$name', age=$age)"
    }

    companion object CREATOR : Parcelable.Creator<Person> {
        override fun createFromParcel(parcel: Parcel): Person {
            return Person(parcel)
        }

        override fun newArray(size: Int): Array<Person?> {
            return arrayOfNulls(size)
        }
    }


}
```

- 创建了一个实体类，还需要创建对应的AIDL文件

  - 文件名和实体类名相同

  - 使用parcelable修饰

    - 注意parcelable的首字母是小写

    ```
    package com.flyaudio.aidltest.aidl;
    
    // Declare any non-default types here with import statements
    
    parcelable Person;
    ```

    

#### 服务端

###### 作用: 提供接口供其它进程的程序调用，以便其它进程的程序可以使用本程序的某些功能，并且服务端程序可以提供接口给其它进程的程序以便异步获取结果

- 创建一个AIDL文件

  - 在里面定义需要提供给外界调用的方法

  - 该文件中用到的自定义的实体类或接口等都需要手动导包

  - AIDL文件写好之后要手动编译一下项目才能生成对应的.java文件

    【Build】 --> 【Make Project】

  ```java
  // IMyAidlInterface.aidl
  package com.flyaudio.aidltest.aidl;
  
  // Declare any non-default types here with import statements
  import com.flyaudio.aidltest.aidl.Person;
  import com.flyaudio.aidltest.aidl.IAidlListener;
  
  interface IMyAidlInterface {
  
      //计算两个数的和
      int add(int a,int b);
  
      List<Person> addPerson(in Person person);
  
      void registerListener(IAidlListener listener);
  
      void unregisterListener(IAidlListener listener);
  
      void reques
          tArticleInfo();
  }
  
  ```

- 创建一个Service类

  - 提供给其它进程的程序进行绑定

  - 在Service中真正实现对外提供的接口
  - Binder的实现是实现的AIDL的Stub对象

  ```kotlin
  class MyAidlService : Service() {
  
      //类型必须是ArrayList
      private var mPersons: ArrayList<Person> = ArrayList()
  
      private val mBinder: IBinder =
          MyAidlBinder()
  
      private val mRemoteCallbackList: RemoteCallbackList<IAidlListener> = RemoteCallbackList()
  
      override fun onBind(intent: Intent?): IBinder? {
          mPersons = ArrayList()
          //返回Binder对象
          return mBinder
      }
  
      //实现的是IMyAidlInterface.Stub()
      inner class MyAidlBinder : IMyAidlInterface.Stub(){
          override fun registerListener(listener: IAidlListener) {
              mRemoteCallbackList.register(listener)
          }
  
          override fun unregisterListener(listener: IAidlListener) {
              mRemoteCallbackList.unregister(listener)
          }
  
          override fun addPerson(person: Person): MutableList<Person> {//<---看这里
              mPersons.add(person)
              return mPersons
          }
  
          override fun add(a: Int, b: Int): Int {//<---看这里
              Log.d("TAG","收到了远程的请求：输入的参数是:a=${a},b=${b}")
              return a+b
          }
  
          override fun requestArticleInfo() {//<---看这里
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
  ```

  

- 在AndroidManifest.xml中注册Service

  - **特别注意：Service的包名要填完整的路径**

  - 添加export属性 ：android:exported="true"

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="com.flyaudio.aidltest">
    
        <uses-permission android:name="android.permission.INTERNET"/>
        <application
            android:allowBackup="true"
            android:icon="@mipmap/ic_launcher"
            android:label="@string/app_name"
            android:roundIcon="@mipmap/ic_launcher_round"
            android:supportsRtl="true"
            android:theme="@style/AppTheme"
            >
    
        <activity android:name=".MainActivity">
                <intent-filter>
                    <action android:name="android.intent.action.MAIN" />
    
                    <category android:name="android.intent.category.LAUNCHER" />
                </intent-filter>
            </activity>
           //看这里
            //一定要填完整的路径：com.flyaudio.aidltest.MyAidlService
            <service android:name="com.flyaudio.aidltest.MyAidlService"
                android:exported="true"
                />
        </application>
    
    </manifest>
    ```

    

#### 客户端

- 将服务端定义的AIDL文件和实体类全部拷贝过来

  - **包名要完全一致**

  - **包名要完全一致**

  - **包名要完全一致**

  - 如图所示

    <img src="C:\Users\user\AppData\Roaming\Typora\typora-user-images\image-20200814170920394.png" alt="image-20200814170920394" style="zoom:50%;" />

- 在客户端绑定服务端定义的Service

  - Service类的包名和类名要与服务端定义的完全一致，类名要写完整路径

    ```koltin
    private fun bindService() {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.flyaudio.aidltest",//Service类所在的包名
                    "com.flyaudio.aidltest.MyAidlService"//Service的完整路径
                )
    
            }
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }
    ```

  - 实现ServiceConnection接口并获取Binder对象（就是服务端的Service的onBind()返回的Binder对象）

    ```kotlin
       private var mIMyAidlInterface: IMyAidlInterface? = null
       
       private val mServiceConnection: ServiceConnection = object :ServiceConnection{
       		//断开服务的时候调用
            override fun onServiceDisconnected(name: ComponentName?) {
                mIMyAidlInterface?.unregisterListener(mIAidlListener)
                mIMyAidlInterface = null
            }
    
    		//连接服务的时候调用
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Log.d("TAG","客户端服务连接成功")
                //获取服务Binder对象（就是服务端的Service的onBind()返回的Binder对象）
                mIMyAidlInterface = IMyAidlInterface.Stub.asInterface(service)
                mIMyAidlInterface!!.registerListener(mIAidlListener)
            }
    
        }
    ```

    

  - 解绑服务

    ```koltin
    override fun onDestroy() {
            super.onDestroy()
            unbindService(mServiceConnection)
        }
    ```

- 在客户端调用服务端提供的API

  ```kotlin
  override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          setContentView(R.layout.activity_main)
  
          //绑定服务
          bindService()
  
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
                  //调用远程的服务
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
              //调用远程的服务(异步请求)
              mIMyAidlInterface?.requestArticleInfo()
          }
      }
  ```



-----------------------------------------------------------------------------------------------------------------------
