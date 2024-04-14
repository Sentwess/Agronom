package com.example.agronom.data

data class Harvest(var docId : String ?= null,
                   var culture : Map<String,String>? = null,
                   var field : Map<String,String>? = null,
                   var sowing : Map<String,Any>? = null,
                   var count : Double ?= null,
                   var date : String ?= null)
