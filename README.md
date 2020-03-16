### API Endpoints

| Method | URI | Description |
| ------ | --- | ----------- |
| GET    |/               | swagger-ui |
| POST   |/api/v1/barista | create a new barista |

### Development Environment

#### API Examples

Create a new Barista
```
curl -X POST "http://localhost:8070/api/v1/barista" -H "accept: application/json" -H "Content-Type: application/json" -d "{\"email\":\"raphael.alex@gmail.com\",\"name\":\"raphael abreu\"}"
```

List all Baristas
```
curl -X GET "http://localhost:8070/api/v1/barista" -H "accept: application/json" -H "Content-Type: application/json"
```

Create a new Order
```
curl --location --request POST 'http://localhost:8070/api/v1/order' \
--header 'Content-Type: application/json' \
--data-raw '{
  "items": [
    {
      "description": "espresso",
      "portionType": "units",
      "portions": 1,
      "size": "S"
    },
    {
      "description": "red velvet cake",
      "portionType": "slices",
      "portions": 1,
      "size": "NA"
    }
  ],
  "paymentMethod": "CREDIT_CARD",
  "ssn": "123456789"
}'
```

Get all orders
```
curl --location --request GET 'http://localhost:8070/api/v1/order' \
--header 'Accept: application/json'
```

Create a new Maintenance
```
curl --location --request POST 'http://localhost:8070/api/v1/maintenance' \
--header 'Content-Type: application/json' \
--data-raw '{
  "barista": "12345"
}'
```

Get all Maintenance records
```
curl --location --request GET 'http://localhost:8070/api/v1/maintenance'
```

Add coffee/milk to storage
```
curl --location --request POST 'http://localhost:8070/api/v1/storage' \
--header 'Content-Type: application/json' \
--data-raw '{
  "milk": 100.0,
  "coffee": 14,
  "op": "IN"
}'
```

Withdraw coffee/milk of storage
```
curl --location --request POST 'http://localhost:8070/api/v1/storage' \
--header 'Content-Type: application/json' \
--data-raw '{
  "milk": 100.0,
  "coffee": 14,
  "op": "OUT",
  "transaction": "12345"
}'
```

Check storage availability
```
curl --location --request GET 'http://localhost:8070/api/v1/storage'
```


```
mvn clean package fabric8:deploy -Popenshift
mvn clean package fabric8:deploy -Popenshift -DskipTests
mvn clean package thorntail:run
```
