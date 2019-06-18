# k8s 部署 springboot 2.x

### 本地测试

#### 本地`gradle`打`jar`包，并测试

- `gradle build`对应的打包目录 --> `build`目录下的`libs`文件夹下会出现 `spring-boot-scaffold-1.0.jar`
    
- 测试

```shell
java -jar spring-boot-scaffold-1.0.jar
```
  
  
**浏览器访问：http://localhost:8080/swagger-ui.html ，如成功运行，请接着往下看**


* * *

### Docker镜像

#### 安装 docker 环境

```shell
brew cask install docker
```

#### 配置 `build.gradle`

##### 引入docker对应的插件

```gradle
buildscript {
    ext {
        springBootVersion = '2.1.1.RELEASE'
    }
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
        mavenCentral()
    }
    dependencies {
        classpath('gradle.plugin.com.palantir.gradle.docker:gradle-docker:0.13.0')
    }
}

apply plugin: 'application'
apply plugin: 'com.palantir.docker'
```

##### 配置打包后的文件格式
```gradle
bootJar {
    baseName = 'spring-boot-scaffold'
    version = '0.1.0'
}

task unpack(type: Copy) {
    dependsOn bootJar
    from(zipTree(tasks.bootJar.outputs.files.singleFile))
    into("build/dependency")
}

docker {
    name "${project.group}/${bootJar.baseName}"
    copySpec.from(tasks.unpack.outputs).into("dependency")
    buildArgs(['DEPENDENCY': "dependency"])
}
```

##### docker配置完毕，完整的build.gradle配置文件如下：

```gradle
buildscript {
    ext {
        springBootVersion = '2.1.1.RELEASE'
    }
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")// 让spring-boot支持gradle
        classpath('gradle.plugin.com.palantir.gradle.docker:gradle-docker:0.13.0')
    }
}

apply plugin: 'java'
apply plugin: 'idea'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'
apply plugin: 'application'
apply plugin: 'com.palantir.docker'

group = 'padiapta'
version = '1.0'

mainClassName = 'com.yipage.root.SpringBootRun'

bootJar {
    baseName = 'spring-boot-scaffold'
    version = '1.0'
}

// 强制使用4.9版本，团队规范
task wrapper(type: Wrapper) {
    gradleVersion = '4.9'
}

tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}

task unpack(type: Copy) {
    dependsOn bootJar
    from(zipTree(tasks.bootJar.outputs.files.singleFile))
    into("build/dependency")
}

docker {
    name "${project.group}/${bootJar.baseName}"
    copySpec.from(tasks.unpack.outputs).into("dependency")
    buildArgs(['DEPENDENCY': "dependency"])
}

repositories {
    mavenCentral()
}

sourceCompatibility = 1.8
targetCompatibility = 1.8

dependencies {
    compile group: 'org.springframework.boot', name: 'spring-boot-starter-web', version: '2.1.3.RELEASE'
    compile group: 'org.springframework.boot', name: 'spring-boot-starter-actuator', version: '2.1.3.RELEASE'
    compile group: 'org.springframework.boot', name: 'spring-boot-starter-aop', version: '2.1.3.RELEASE'
    compile group: 'com.github.pagehelper', name: 'pagehelper-spring-boot-starter', version: '1.2.10'
    testCompile group: 'org.springframework.boot', name: 'spring-boot-starter-test', version: '2.1.3.RELEASE'
}
```

##### 在根目录下创建 `Dockerfile`

```docker
FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG DEPENDENCY=build/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-cp","app:app/lib/*","com.yipage.root.SpringBootRun"]
```

##### 生成对应镜像

```shell
docker build -t padipata/spring-boot-scaffold:latest .
docker images
```

- 查看构建好的镜像

![6b97fd80adab834c617c9a0907c6b973.png](evernotecid://528174BA-8B48-4031-9CF5-033445ED73DE/appyinxiangcom/21727740/ENResource/p25)

#### 运行镜像

- `docker run -p 8080:8080 docker padipata/spring-boot-scaffold:latest`


**浏览器访问：http://localhost:8080/swagger-ui.html ，如成功运行，请接着往下看**

* * *


### 在 `Kubernetes` 上运行

##### 安装 `Kubernetes` 运行环境

- 安装 virtualbox

```shell
brew cask install virtualbox
```

- 安装 minikube

```shell
curl -Lo minikube http://kubernetes.oss-cn-hangzhou.aliyuncs.com/minikube/releases/v1.0.1/minikube-darwin-amd64

chmod +x minikube

mv minikube /usr/local/bin/

# 启动
minikube start --registry-mirror=https://registry.docker-cn.com
```

- 打开k8s UI管理界面（方便看状态）
```shell
minikube dashboard
```

#### 正式开始部署

```shell
# 将刚才构建的镜像导出到一个tar包里
docker save springbootdemo:latest > springbootdemo.tar

# 将 docker daemon 切换成 minikube 的
eval $(minikube docker-env)

# 在项目根目录下创建一个名为 spring-boot-scaffold.yaml 的文件，这是容器编排的脚本
touch spring-boot-scaffold.yaml
vim spring-boot-scaffold.yaml
```

写入以下内容：

```yaml
apiVersion: apps/v1beta1
kind: Deployment
metadata:
  name: spring-boot-scaffold-deployment
  labels:
    app: spring-boot-scaffold
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-boot-scaffold
  template:
    metadata:
      labels:
        app: spring-boot-scaffold
    spec:
      containers:
        - name: spring-boot-scaffold
          image: padiapta/spring-boot-scaffold:latest # 对应本地镜像名字
          imagePullPolicy: Never # 只用于测试，不建议用于生产！！！具体看下面说明
          ports:
            - containerPort: 8080
```

#### imagePullPolicy说明

> 默认值,总是拉取 pull

```shell
imagePullPolicy: Always
```

> 本地有则使用本地镜像,不拉取

```shell
imagePullPolicy: IfNotPresent
```

> 只使用本地镜像，从不拉取

```shell
imagePullPolicy: Never
```

#### 创建k8s集群

- 将我们的 `SpringBoot` 的应用的集群给创建出来（报错请看下面的常见错误）

```shell
kubectl apply -f spring-boot-scaffold.yaml
```

```shell
# 将端口暴露出来
kubectl expose deployment spring-boot-scaffold-deployment --type=NodePort

# 得到访问的 URL
minikube service spring-boot-scaffold-deployment --url
```



***

### kubernetes常用操作

- 查看pod节点
```shell
kubectl get pods
```

- 删除服务
```shell
kubectl delete services spring-boot-scaffold-deployment
```

- 删除应用
```
kubectl delete deployment spring-boot-scaffold-deployment
```

***

### 服务扩容

在服务的运行过程中，有时我们可能会面临服务的消费者过多，对服务产生很大压力的场景。这个时候，我们就需要对这个服务做弹性扩容。

运行如下命令：
```shell
kubectl get pods
```

我们可以看到，现在xxx这个服务，有两个实例在运行，下面我将它扩充到4个：
```shell
kubectl scale deployment spring-boot-scaffold-deployment --replicas=4
```

再次执行`kubectl get pods`，可以看到服务成功扩充到了4个节点。

**浏览器访问：http://192.168.99.100:30583/swagger-ui.html ，如成功运行，则说明部署成功**

* * *
### 常见错误

##### kubectl apply -f 创建集群应用时报错

```shell
# kubectl从docker运行，它在我们安装Docker时预先安装。您可以使用以下命令检查这一点
ls -l $(which kubectl) 

# 如果是Docker环境，返回为:
/usr/local/bin/kubectl - > /Applications/Docker.app/Contents/Resources/bin/kubectlcode。

# 现在我们必须使用brew安装的kubectl覆盖符号链接
rm /usr/local/bin/kubectl
brew link --overwrite kubernetes-cli

# 查看是否成功
ls -l $(which kubectl)
```