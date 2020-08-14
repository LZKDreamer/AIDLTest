package com.flyaudio.aidltest.aidl

import android.os.Parcel
import android.os.Parcelable

/**
 * Author: LiaoZhongKai
 * Date: 2020/8/14 12:54
 * Description:
 */
data class Person(val name: String,val age: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
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