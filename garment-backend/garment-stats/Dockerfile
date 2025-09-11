FROM openjdk:17-jdk-slim

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 安装curl用于健康检查
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 创建应用目录
WORKDIR /app

# 复制JAR文件
COPY target/garment-stats-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 暴露端口
EXPOSE 8080

# 设置JVM参数和启动命令
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]