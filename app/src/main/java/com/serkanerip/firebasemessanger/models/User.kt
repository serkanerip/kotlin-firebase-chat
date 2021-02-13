package com.serkanerip.firebasemessanger.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(var uid:String, var username:String, var profileImageUrl: String) : Parcelable {
    constructor() : this("", "", "")
}