# ============================================================
# Run this script AS ADMINISTRATOR to enable TCP/IP for SQL Server
# Right-click PowerShell -> Run as Administrator -> paste & run
# ============================================================

Write-Host "=== Enabling TCP/IP for SQL Server ===" -ForegroundColor Cyan

# Enable TCP/IP protocol
$regPath = "HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\MSSQL16.MSSQLSERVER\MSSQLServer\SuperSocketNetLib\Tcp"
Set-ItemProperty -Path $regPath -Name "Enabled" -Value 1
Write-Host "[OK] TCP/IP protocol enabled" -ForegroundColor Green

# Ensure port 1433
$ipAllPath = "$regPath\IPAll"
Set-ItemProperty -Path $ipAllPath -Name "TcpPort" -Value "1433"
Set-ItemProperty -Path $ipAllPath -Name "TcpDynamicPorts" -Value ""
Write-Host "[OK] Port 1433 configured" -ForegroundColor Green

# Restart SQL Server
Write-Host "Restarting SQL Server..." -ForegroundColor Yellow
Restart-Service -Name "MSSQLSERVER" -Force
Start-Sleep -Seconds 5

# Verify
$result = Test-NetConnection -ComputerName localhost -Port 1433 -InformationLevel Quiet
if ($result) {
    Write-Host "[OK] SQL Server is now accepting TCP connections on port 1433!" -ForegroundColor Green
} else {
    Write-Host "[FAIL] Still cannot connect. Try restarting computer." -ForegroundColor Red
}
