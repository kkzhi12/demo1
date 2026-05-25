$ProgressPreference = 'SilentlyContinue'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

# Test eastmoney main financial data API for ICBC (601398)
$url = "https://datacenter.eastmoney.com/securities/api/data/get?type=RPT_F10_FINANCE_MAINFINADATA_BANK&sty=ALL&filter=(SECUCODE=%22601398.SH%22)&p=1&ps=5&sr=-1&st=REPORT_DATE&source=HSF10&client=PC"

try {
    $response = Invoke-RestMethod -Uri $url -Headers @{
        "User-Agent" = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        "Referer" = "https://emweb.eastmoney.com/"
    }
    Write-Host "=== API 1 Result ==="
    $response | ConvertTo-Json -Depth 4 | Out-File -FilePath "c:\code\demo1\api1_result.json" -Encoding UTF8
    Write-Host "Saved to api1_result.json"
    Write-Host "Success:" $response.success
    Write-Host "Code:" $response.code
    if ($response.result -and $response.result.data) {
        Write-Host "Data count:" $response.result.data.Count
        $first = $response.result.data[0]
        Write-Host "First record keys:" ($first.PSObject.Properties.Name -join ", ")
    } else {
        Write-Host "No result data"
        Write-Host "Full response:" ($response | ConvertTo-Json -Depth 2)
    }
} catch {
    Write-Host "API 1 Error:" $_.Exception.Message
}

Write-Host ""
Write-Host "=== Testing API 2 - General financial indicators ==="

# Try the general financial indicator API
$url2 = "https://datacenter.eastmoney.com/securities/api/data/v1/get?reportName=RPT_F10_FINANCE_MAINFINADATA&columns=ALL&filter=(SECUCODE=%22601398.SH%22)&pageNumber=1&pageSize=5&sortTypes=-1&sortColumns=REPORT_DATE"

try {
    $response2 = Invoke-RestMethod -Uri $url2 -Headers @{
        "User-Agent" = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        "Referer" = "https://emweb.eastmoney.com/"
    }
    Write-Host "Success:" $response2.success
    Write-Host "Code:" $response2.code
    $response2 | ConvertTo-Json -Depth 4 | Out-File -FilePath "c:\code\demo1\api2_result.json" -Encoding UTF8
    Write-Host "Saved to api2_result.json"
    if ($response2.result -and $response2.result.data) {
        Write-Host "Data count:" $response2.result.data.Count
        $first2 = $response2.result.data[0]
        Write-Host "First record keys:" ($first2.PSObject.Properties.Name -join ", ")
    } else {
        Write-Host "No result data"
        Write-Host "Full response:" ($response2 | ConvertTo-Json -Depth 2)
    }
} catch {
    Write-Host "API 2 Error:" $_.Exception.Message
}
