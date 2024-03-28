package com.example.agronom.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cultures(var docId : String ?= null,
                    var cultureName : String ?= null,
                    var varienty : String ?= null,
                    var boardingMonth : String ?= null,
                    var growingSeason : String ?= null,
                    var imagePath : String ?= null) : Parcelable
