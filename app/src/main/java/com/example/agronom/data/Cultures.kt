package com.example.agronom.data

data class Cultures(var id : Int ?= null, var cultureName : String ?= null,var varienty : String ?= null,
                    var boardingMonth : String ?= null, var growingSeason : String ?= null, var imagePath : String ?= null)
