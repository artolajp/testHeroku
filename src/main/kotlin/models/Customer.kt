package models

import kotlinx.serialization.Serializable

val customerStorage = mutableListOf<Customer>()

@Serializable
data class Customer(val id: Int, val firstName: String, val lastName: String, val email: String)