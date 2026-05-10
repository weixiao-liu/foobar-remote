# Foobar Remote - Android App 构建说明

## 项目位置
`C:\Users\Administrator\.qclaw\workspace-agent-d2015cd3\FoobarRemote\`

## 功能
- 内置 WebView 加载 `http://192.168.1.6:8888/albumart_minimal/player.html`
- 浮动麦克风按钮，点击说话
- 支持语音指令：
  - **播放** → 点击播放/暂停按钮
  - **暂停** → 点击播放/暂停按钮
  - **声音大一点 / 大声 / 音量加** → 音量 +10
  - **声音小一点 / 小声 / 音量减** → 音量 -10
  - **上一曲 / 上一首** → 上一曲
  - **下一曲 / 下一首** → 下一曲

## 构建方式

### 方式一：Android Studio（推荐）
1. 打开 Android Studio
2. File → Open → 选择 `FoobarRemote` 文件夹
3. 等待 Gradle 同步完成
4. 连接手机或启动模拟器
5. 点击 Run ▶️

### 方式二：命令行构建
```bash
cd FoobarRemote
.\gradlew.bat assembleDebug
```
生成的 APK 在：
`app\build\outputs\apk\debug\app-debug.apk`

## 安装到手机
```bash
adb install app\build\outputs\apk\debug\app-debug.apk
```

## 注意事项
- 手机和电脑必须在同一局域网（192.168.1.x）
- foobar2000 + foo_httpcontrol 需正在运行
- 首次使用需授权麦克风权限
- 如 IP 地址变化，修改 `MainActivity.java` 中的 `PLAYER_URL`

## 文件清单
```
FoobarRemote/
├── build.gradle              # 项目级构建配置
├── settings.gradle           # 项目设置
├── gradle.properties        # Gradle 属性
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── app/
    ├── build.gradle         # App 级构建配置
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/foobar/remote/MainActivity.java
        └── res/
            ├── layout/activity_main.xml
            ├── values/strings.xml
            └── values/styles.xml
```
