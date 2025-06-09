// 依赖版本管理文件
// 将此内容添加到 build.gradle.kts 文件顶部

extra["versions"] = mapOf(
    // Apache Commons 库
    "commonsBeanutils" to "1.11.0",
    "commonsCodec" to "1.18.0", 
    "commonsCollections" to "3.2.2",
    "commonsLang" to "2.6",
    "commonsLogging" to "1.3.5",
    
    // JSON 处理
    "ezmorph" to "1.0.6",
    "jsonLib" to "2.4",
    
    // 日志
    "log4j" to "1.2.17",
    
    // 数据库
    "derby" to "10.17.1.0",
    
    // 静态分析工具
    "findsecbugs" to "1.14.0"
)

// 推荐的现代化依赖替换版本
extra["modernVersions"] = mapOf(
    // 现代化替换建议
    "commonsLang3" to "3.13.0",
    "commonsCollections4" to "4.4",
    "log4j2Core" to "2.20.0",
    "jacksonDatabind" to "2.15.2",
    "slf4jApi" to "2.0.7",
    "logbackClassic" to "1.4.8"
)
