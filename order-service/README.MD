## Access to H2-Console
if your MS is running on 9095 port then access below link - 
````
http://localhost:9095/h2-console/login.do
````

run below sql -

````
SELECT
o.id,
o.user_id,
o.product_id,
o.amount,
s.status,
s.created_at,s.reason
FROM orders o
INNER JOIN orders_status_details s
ON o.id = s.order_id;

````

## Bulk limit test 

````curl
seq 1 20 | xargs -I{} -n1 -P20 curl --location 'http://localhost:9095/orders' \
--header 'Content-Type: application/json' \
--data '{
    "customerId":1001,
    "productId":"P034",
    "quantity":20,
    "price":300.00
}'
````

## resilience4j useful configuration.

````yaml
resilience4j:
##  Configure RateLimiter on REST API and ensure that max 5 request would be handled per sec as per below configuration
##  and throw limit exception when it exceeds.
  ratelimiter:
    instances:
      orderRateLimiter:
        timeoutDuration: 0     # If limit exceeded → immediately return failure (no wait)
        limitRefreshPeriod: 1s # Window resets every 1 seconds
        limitForPeriod: 5      # Only 5 calls allowed per window
##  In below configuration, CircuitBreaker analysis last 10 calls using slidingWindowSize,
##  minimumNumberOfCalls:5 - after 5 calls Circuit become OPEN or failureRateThreshold is 50% then OPEN until waitDurationInOpenState: 10sec.
##  else Circuit remain CLOSED
  circuitbreaker:
    instances:
      productCB:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
##  Configure Bulkhead on REST API and ensure that max 5 concurrent request will be accepted without no waiting timeout i.e 0
##  otherwise it failed with too many request.
  bulkhead:
    instances:
      orderServiceBulkhead:
        maxConcurrentCalls: 5
        maxWaitDuration: 0
````

# Certificate for kafka exception testing

## Step 1: Generate a Keystore with an Already Expired Certificate
````json5
keytool -genkeypair \
  -alias expired-server \
  -keyalg RSA \
  -keysize 2048 \
  -keystore expired-keystore.jks \
  -storepass changeit \
  -keypass changeit \
  -dname "CN=localhost, OU=Dev, O=MyCompany, L=City, ST=State, C=IN" \
  -validity 1 \
  -startdate 20200101

````
## Step 2: Export the Certificate (Optional)
````
keytool -exportcert \
-alias expired-server \
-file expired-server.crt \
-keystore expired-keystore.jks \
-storepass changeit
````
## Step 3: Create a Truststore
````
keytool -importcert \
-alias expired-server \
-file expired-server.crt \
-keystore expired-truststore.jks \
-storepass changeit \
-noprompt
````