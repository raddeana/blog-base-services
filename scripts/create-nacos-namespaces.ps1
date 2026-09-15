<#
.SYNOPSIS
  在 Nacos 上创建 blog-base-services 的 dev/test/prod 命名空间（幂等）。
.DESCRIPTION
  通过 Nacos 2.x Open API 创建命名空间，使用自定义短 ID，便于
  通过 NACOS_NAMESPACE 环境变量注入到应用。
  - 已存在的命名空间会被跳过（幂等）。
  - 兼容 PowerShell 5+ 与开启/关闭鉴权的 Nacos 实例。
.PARAMETER NacosAddr
  Nacos 地址（host:port），默认 127.0.0.1:8848。
.PARAMETER Username
  Nacos 登录账号，默认 nacos。留空表示跳过鉴权。
.PARAMETER Password
  Nacos 登录密码，默认 nacos。
.PARAMETER Environments
  要创建的环境名数组，默认 dev,test,prod。
.EXAMPLE
  .\create-nacos-namespaces.ps1
  # 用默认值本地创建 dev/test/prod
.EXAMPLE
  .\create-nacos-namespaces.ps1 -NacosAddr 10.0.0.1:8848 -Username nacos -Password 'secret'
  # 指定远程 Nacos 与鉴权账号
.EXAMPLE
  .\create-nacos-namespaces.ps1 -Environments dev
  # 只创建 dev 命名空间
#>
param(
    [string]$NacosAddr = '127.0.0.1:8848',
    [string]$Username = 'nacos',
    [string]$Password = 'nacos',
    [string[]]$Environments = @('dev', 'test', 'prod')
)

$ErrorActionPreference = 'Stop'
$baseUrl = "http://$NacosAddr/nacos"

# PowerShell 5 不支持 -SkipCertificateCheck；若使用自签证书需在此处放宽校验。
# 本脚本默认走 http，保持默认校验。
[System.Net.ServicePointManager]::SecurityProtocol = `
    [System.Net.SecurityProtocolType]::Tls12 -bor [System.Net.SecurityProtocolType]::Tls13

# 1. 登录（开启鉴权时获取 accessToken；关闭鉴权时返回空 token）
function Get-AccessToken {
    param([string]$user, [string]$pwd)
    if ([string]::IsNullOrEmpty($user)) { return $null }
    $loginUrl = "$baseUrl/v1/auth/users/login"
    try {
        $resp = Invoke-RestMethod -Method Post -Uri $loginUrl `
            -ContentType 'application/x-www-form-urlencoded' `
            -Body @{ username = $user; password = $pwd } `
            -TimeoutSec 10
        if ($resp.accessToken) { return $resp.accessToken }
    } catch {
        # Nacos 关闭鉴权时此接口会 403/401，视为无需鉴权
        Write-Warning "login failed or auth disabled: $($_.Exception.Message)"
    }
    return $null
}

# 2. 查询现有命名空间列表
function Get-Namespaces {
    param([string]$token)
    $url = "$baseUrl/v1/console/namespaces"
    $headers = @{}
    if ($token) { $headers['Authorization'] = "Bearer $token" }
    $resp = Invoke-RestMethod -Method Get -Uri $url -Headers $headers -TimeoutSec 10
    return $resp.data
}

# 3. 创建命名空间
function New-Namespace {
    param(
        [string]$token,
        [string]$nsId,
        [string]$nsName,
        [string]$nsDesc
    )
    $url = "$baseUrl/v1/console/namespaces"
    $headers = @{}
    if ($token) { $headers['Authorization'] = "Bearer $token" }
    # customNamespaceId 让 Nacos 使用调用方提供的 ID 而非随机 UUID
    $body = @{
        customNamespaceId = $nsId
        namespaceName     = $nsName
        namespaceDesc     = $nsDesc
    }
    $resp = Invoke-RestMethod -Method Post -Uri $url -Headers $headers `
        -ContentType 'application/x-www-form-urlencoded' -Body $body -TimeoutSec 10
    return $resp
}

# 主流程
Write-Host "==> Nacos: $NacosAddr"
$token = Get-AccessToken -user $Username -pwd $Password
if ($token) {
    Write-Host "==> auth: ok"
} else {
    Write-Host "==> auth: skipped (disabled or login failed)"
}

$existing = Get-Namespaces -token $token
$existingIds = @{}
foreach ($item in $existing) {
    $existingIds[$item.namespace] = $true
}

$results = @()
foreach ($env in $Environments) {
    $nsId   = "blog-base-$env"
    $nsName = "blog-base-$env"
    $nsDesc = "blog-base-services $env environment"

    if ($existingIds.ContainsKey($nsId)) {
        Write-Host "[skip] $nsId already exists"
    } else {
        $resp = New-Namespace -token $token -nsId $nsId -nsName $nsName -nsDesc $nsDesc
        if ($resp.code -eq 0 -or $resp.code -eq 200) {
            Write-Host "[ok]   created $nsId"
        } else {
            Write-Host "[fail] $nsId : $($resp.message)"
            continue
        }
    }
    $results += [pscustomobject]@{
        Profile      = $env
        NamespaceId  = $nsId
        EnvVar       = "NACOS_NAMESPACE=$nsId"
    }
}

Write-Host "`n==> environment variables for application startup:`n"
$results | Format-Table -AutoSize
