package com.example.agronom.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sowings(var docId : String ?= null,
                   var culture : Map<String,String>? = null,
                   var field : Map<String,String>? = null,
                   var count : Double ?= null,
                   var date : String ?= null,
                   var status : Boolean ?= null) : Parcelable
