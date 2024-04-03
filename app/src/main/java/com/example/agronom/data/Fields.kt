package com.example.agronom.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Fields(var docId : String ?= null,
                    var name : String ?= null,
                    var size : String ?= null,
                    var status : Boolean ?= null, ) : Parcelable
