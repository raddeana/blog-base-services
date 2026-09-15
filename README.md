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

