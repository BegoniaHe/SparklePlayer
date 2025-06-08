# GitHub 工作流使用说明

这个项目包含两个GitHub工作流来管理SparklePlayer的发布。

## 🚀 主要工作流：Build and Release

### 触发方式

1. **标签推送触发**：

   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
2. **手动触发**：

   - 在GitHub仓库页面点击 "Actions" 标签
   - 选择 "Build and Release" 工作流
   - 点击 "Run workflow"
   - 输入版本号（如：v1.0.0）

### 工作流程

1. ✅ **删除所有现有的预发布版本**
2. ✅ **设置Java 8环境**
3. ✅ **使用Gradle编译JAR包**
4. ✅ **准备发布资源**：
   - 复制生成的JAR文件
   - 复制 `sparkleplayer/`目录（包含音频、字体、图标等资源）
   - 创建完整的发布压缩包
5. ✅ **创建新的预发布版本**，包含：
   - JAR文件
   - 完整发布包（ZIP格式）
   - 详细的发布说明

## 🧹 辅助工作流：Clean Prereleases

### 用途

手动清理旧的预发布版本，可以选择保留最新的N个版本。

### 使用方法

- 在GitHub仓库页面点击 "Actions" 标签
- 选择 "Clean Prereleases" 工作流
- 点击 "Run workflow"
- 输入要保留的最新版本数量（默认为1）

## 📁 发布内容

每次发布包含：

1. **JAR文件**：主程序执行文件
2. **SparklePlayer-Release.zip**：完整发布包，包含：
   - JAR文件
   - `sparkleplayer/` 目录及所有资源文件

## 🔧 运行要求

- Java 8 或更高版本
- 运行命令：`java -jar SparklePlayer-*.jar`

## ⚠️ 注意事项

- 所有发布都是**预发布版本**（prerelease）
- 每次新发布会自动删除所有之前的预发布版本
- 工作流需要 `contents: write`权限来创建和删除发布版本
