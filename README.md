# blog-base-services
blog base services, provide base services, include regions, tags and so on

## 环境要求

- JDK 17
- Maven 3.6+
- Spring Boot 3.2.4

## 启动命令

### 方式一：Maven 直接启动（推荐开发环境）

在项目根目录执行：

```bash
mvn spring-boot:run -pl base-service
```

### 方式二：打包后启动 jar（推荐生产环境）

```bash
# 打包（跳过测试以加快速度）
mvn clean package -DskipTests

# 启动
java -jar base-service/target/base-service-1.0.0.jar
```

### 方式三：IDE 启动

直接运行启动类 `com.blog.base.BaseServicesApplication` 的 `main` 方法。

> 默认激活 `local` profile，不依赖 Nacos（服务发现与配置中心均关闭）。

## Nacos 多环境配置

`dev` / `test` / `prod` 环境通过 Nacos 做服务注册发现与配置中心，环境之间使用 **namespace** 隔离，group 统一为 `DEFAULT_GROUP`。

### 环境变量

| 变量 | 说明 | 示例 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | 环境 profile | `dev` / `test` / `prod` |
| `NACOS_ADDR` | Nacos 地址（host:port） | `10.0.0.1:8848` |
| `NACOS_NAMESPACE` | 命名空间 **ID**（非名称，留空为 public） | `9f2b1c...` |
| `NACOS_USERNAME` / `NACOS_PASSWORD` | Nacos 鉴权账号 | `nacos` / `nacos` |

### Nacos dataId 规划

每个环境 namespace 内维护以下配置（group 均为 `DEFAULT_GROUP`，格式 yml）：

- `blog-base-services.yml`：服务公共配置
- `blog-base-services-<profile>.yml`：环境私有配置，应用激活对应 profile 时自动拉取（如 `blog-base-services-dev.yml`）

### 初始化 Nacos 命名空间

项目提供幂等脚本，自动创建 `blog-base-dev` / `blog-base-test` / `blog-base-prod` 三个命名空间（自定义短 ID，可直接作为 `NACOS_NAMESPACE` 值）。已存在的命名空间会被跳过，可重复执行。

**Windows PowerShell：**

```powershell
# 默认本地 Nacos（127.0.0.1:8848），账号 nacos/nacos
.\scripts\create-nacos-namespaces.ps1

# 指定远程 Nacos 与鉴权
.\scripts\create-nacos-namespaces.ps1 -NacosAddr 10.0.0.1:8848 -Username nacos -Password secret

# 仅创建 dev 命名空间
.\scripts\create-nacos-namespaces.ps1 -Environments dev
```

**Linux / macOS / CI：**

```bash
chmod +x scripts/create-nacos-namespaces.sh
# 用法: ./scripts/create-nacos-namespaces.sh [NACOS_ADDR] [USERNAME] [PASSWORD] [ENV1,ENV2,...]
./scripts/create-nacos-namespaces.sh 10.0.0.1:8848 nacos secret dev,test
```

脚本执行后会输出每个环境对应的 `NACOS_NAMESPACE=...` 值，可直接复制到部署环境变量。

> 注：脚本依赖 Nacos 2.2+ 的 `customNamespaceId` 参数以生成自定义 ID；Nacos 2.0/2.1 会返回 UUID 形式 ID，需从控制台读取后手动设置 `NACOS_NAMESPACE`。

### 各环境启动示例

Linux / macOS：

```bash
export SPRING_PROFILES_ACTIVE=dev
export NACOS_ADDR=10.0.0.1:8848
export NACOS_NAMESPACE=blog-base-dev
mvn spring-boot:run -pl base-service
```

Windows PowerShell：

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:NACOS_ADDR="10.0.0.1:8848"
$env:NACOS_NAMESPACE="blog-base-dev"
mvn spring-boot:run -pl base-service
```

或打包后通过 JVM 参数传入（跨平台，`-D` 参数须位于 `-jar` 之前）：

```bash
java -Dspring.profiles.active=prod \
     -DNACOS_ADDR=10.0.0.1:8848 \
     -DNACOS_NAMESPACE=blog-base-prod \
     -jar base-service/target/base-service-1.0.0.jar
```

## 测试命令

### 运行全部测试

```bash
mvn test
```

### 运行指定模块测试

```bash
mvn test -pl base-service
```

### 运行单个测试类

```bash
mvn test -pl base-service -Dtest=SomeTestClass
```

### 运行单个测试方法

```bash
mvn test -pl base-service -Dtest=SomeTestClass#methodName
```

