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

    void requestArticleInfo();
}
