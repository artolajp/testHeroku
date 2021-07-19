package persistence

import models.Customer
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object CustomersTable : IntIdTable(){
    val firstName = varchar("firstName", 50)
    val lastName = varchar("lastName", 50)
    val email = varchar("email", 50)
}

class CustomerEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CustomerEntity>(CustomersTable)

    var firstName by CustomersTable.firstName
    var lastName by CustomersTable.lastName
    var email by CustomersTable.email

    fun toCustomer(): Customer {
        return Customer(id.value,firstName,lastName,email)
    }
}