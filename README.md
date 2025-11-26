## ecom-saga-microservice
### modules
 1.) Order Servie

 2.) Inventory Service

 3.) Payment Service

 4.) Shipping Service.

### Design flow

<img alt="img.png" src="design-flow.png"/>

## Place an oder through REST Endpoint - 
````curl
curl --location 'http://localhost:9095/orders' \
--header 'Content-Type: application/json' \
--data '{
    "customerId":1001,
    "productId":"P038",
    "quantity":3,
    "price":300.00
}'
````
### Track order
```curl
syntex- http://localhost:9095/orders/<order_id>
curl --location 'http://localhost:9095/orders/a314ecb7-393d-4f31-a34a-bd10579fe3f3'
```
response
````json5
{
    "transactionId": "cd48a7f4-4de1-452c-bff4-4d0ad6983668",
    "shippingId": "5a6e4d3f-2a4b-46f7-b335-d53e1f3b681a",
    "currentStatus": "SHIPPED_READY",
    "totalAmount": 8997.0,
    "orderStatusDetails": [
        {
            "id": 1,
            "reason": "",
            "status": "PENDING",
            "created_date": "2025-11-26T15:04:23.365193"
        },
        {
            "id": 2,
            "reason": "INVENTORY_RESERVED",
            "status": "INVENTORY_RESERVED",
            "created_date": "2025-11-26T15:04:24.380929"
        },
        {
            "id": 3,
            "reason": "PAYMENT_SUCCESS",
            "status": "PAYMENT_PENDING",
            "created_date": "2025-11-26T15:04:25.044508"
        },
        {
            "id": 4,
            "reason": "PAYMENT_SUCCESS",
            "status": "PAYMENT_COMPLETED",
            "created_date": "2025-11-26T15:04:25.057013"
        },
        {
            "id": 5,
            "reason": "SHIPPED_READY",
            "status": "SHIPPED_READY",
            "created_date": "2025-11-26T15:05:26.564712"
        }
    ],
    "created_date": "2025-11-26T15:04:23.365169",
    "orderId": "a314ecb7-393d-4f31-a34a-bd10579fe3f3",
    "products": [
        {
            "productId": "P038",
            "quantity": 3,
            "productDetails": "LEGO Classic Brick Set",
            "pricePerUnit": 2999.0
        }
    ]
}
````
## Fetch Product Details from Inventory Service
````curl
curl --location 'http://localhost:9097/product/P038'
````
response 
```json5
{
    "id": 38,
    "productId": "P038",
    "productDetails": "LEGO Classic Brick Set",
    "availableQuantity": 35,
    "pricePerUnit": 2999.00,
    "updatedAt": "2025-11-26T15:07:34.513123",
    "createdAt": "2025-11-26T15:03:37.526352"
}
```
## Update the shipping order status
Possible Shipping status - 
````json5
SHIPPED,
SHIPPED_READY,
SHIPPED_CANCELLED,
SHIPMENT_FAILED
````
```curl
curl --location 'http://localhost:9099/shipping/<shipping_id>/<shipping_status>'
curl --location 'http://localhost:9099/shipping/5a6e4d3f-2a4b-46f7-b335-d53e1f3b681a/SHIPPED_CANCELLED'
```


