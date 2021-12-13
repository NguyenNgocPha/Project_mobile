package com.example.motobike.listener

import com.example.motobike.model.CartModel

interface ICartLoadListener {
    fun onLoadCardSuccess(cartModelList: List<CartModel>)
    fun onLoadCartFailed(message:String?)
}