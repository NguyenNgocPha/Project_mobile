package com.example.motobike.listener

import com.example.motobike.model.DrinkModel

interface IDrinkLoadListener {
    fun onDrinkloadSuccess(drinkModelList:List<DrinkModel>)
    fun onDrinkloadFailed(message:String?)
}