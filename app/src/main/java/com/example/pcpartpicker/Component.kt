package com.example.pcpartpicker

class Component {
    data class Part(
        val name: String,
        val url: String,
        val price: String,
        val image: String
    )

    data class Product(
        val name: String,
        val specs: List<Spec>,
        val priceList: List<Price>
    )

    data class Spec(
        val key: String,
        val value: String
    )

    data class Price(
        val seller: String,
        val value: Double
    )

}