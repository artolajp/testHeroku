package persistence

import models.Customer
import models.Order
import models.OrderItem
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object OrdersTable : IntIdTable(){
}

class OrderEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OrderEntity>(OrdersTable)

    var number by OrdersTable.id
    val items by OrderItemEntity referrersOn OrderItemsTable.order

    fun toOrder(): Order {
        return Order(id.value.toString(), items.map { it.toOrderItem()}.toList() )
    }
}

object OrderItemsTable : IntIdTable(){
    val item = varchar("item", 50)
    val amount = integer("amount")
    val price =  decimal("price",10,2)
    val order = reference("order", OrdersTable)
}

class OrderItemEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OrderItemEntity>(OrderItemsTable)

    var item by OrderItemsTable.item
    var amount by OrderItemsTable.amount
    var price by OrderItemsTable.price

    fun toOrderItem(): OrderItem {
        return OrderItem(item,amount,price.toDouble())
    }
}
