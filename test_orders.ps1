try {
    Write-Host "Fetching Products..."
    $productsResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/products" -UseBasicParsing
    $products = $productsResponse.Content | ConvertFrom-Json
    
    if ($products.Count -eq 0) {
        Write-Error "No products found!"
        exit 1
    }
    
    $productId = $products[0].id
    Write-Host "Using Product ID: $productId"
} catch {
    Write-Error "Failed to fetch products: $_"
    exit 1
}

$headers = @{ "Content-Type" = "application/json" }
$body = @{
    userId = "test_user_123"
    shippingAddress = "123 Test St"
    stripePaymentId = "pi_test_123"
    items = @(
        @{
            id = $productId
            quantity = 1
            size = "M"
            color = "Blue"
        }
    )
} | ConvertTo-Json -Depth 5

Write-Host "Creating Order..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/orders" -Method Post -Headers $headers -Body $body -UseBasicParsing
    Write-Host "Order Created: $($response.StatusCode)"
    Write-Host $response.Content
} catch {
    Write-Host "Create Failed: $_"
    if ($_.Exception.Response) {
        Write-Host "Status: " $_.Exception.Response.StatusCode.value__
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Body: $responseBody"
    }
}

Write-Host "`nGetting User Orders..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/orders/user/test_user_123" -UseBasicParsing
    Write-Host "User Orders: $($response.Content)"
} catch {
    Write-Host "Get Failed: $_"
     if ($_.Exception.Response) {
        Write-Host "Status: " $_.Exception.Response.StatusCode.value__
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Body: $responseBody"
    }
}
