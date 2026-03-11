$baseUrl = "http://localhost:8080/api"

function Get-Category-Id {
    param([string]$name)
    try {
        $cats = Invoke-RestMethod -Uri "$baseUrl/categories" -Method Get
        foreach ($c in $cats) {
            if ($c.name -eq $name) {
                return $c.id
            }
        }
    } catch {
        Write-Host "Error fetching categories: $_"
    }
    return $null
}

function Create-Category {
    param([string]$name, [string]$desc, [string]$img)
    $body = @{
        name = $name
        description = $desc
        imageUrl = $img
    } | ConvertTo-Json
    
    try {
        $res = Invoke-RestMethod -Uri "$baseUrl/categories" -Method Post -Body $body -ContentType "application/json"
        Write-Host "Created Category: $name (ID: $($res.id))"
        return $res.id
    } catch {
        Write-Host "Error creating category $name : $_"
        return $null
    }
}

function Create-Product {
    param([string]$name, [string]$catId, [string]$price, [string]$sku)
    $body = @{
        name = $name
        description = "Premium quality $name"
        price = $price
        originalPrice = [double]$price * 1.2
        categoryId = $catId
        stock = 100
        imageUrl = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=500"
        brand = "Jay Shree"
        sizes = "Free Size"
        colors = "Red, Gold"
        material = "Silk"
        isFeatured = $true
    } | ConvertTo-Json

    try {
        $res = Invoke-RestMethod -Uri "$baseUrl/products" -Method Post -Body $body -ContentType "application/json"
        Write-Host "Created Product: $name (ID: $($res.id))"
    } catch {
        Write-Host "Error creating product $name : $_"
    }
}

# 1. Provide "Sarees"
$sareesId = Get-Category-Id "Sarees"
if (-not $sareesId) {
    $sareesId = Create-Category "Sarees" "Traditional Sarees" "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=500"
} else {
    Write-Host "Existing Category: Sarees (ID: $sareesId)"
}

if ($sareesId) {
    Create-Product "Kanjivaram Silk Saree" $sareesId "12000"
}

# 2. Provide "Kids Ethnic"
$kidsId = Get-Category-Id "Kids Ethnic"
if (-not $kidsId) {
    $kidsId = Create-Category "Kids Ethnic" "Kids Wear" "https://images.unsplash.com/photo-1503944583220-79d8926ad5e2?w=500"
} else {
    Write-Host "Existing Category: Kids Ethnic (ID: $kidsId)"
}

if ($kidsId) {
    Create-Product "Boys Kurta Pajama" $kidsId "1500"
}

Write-Host "Seeding script finished."
