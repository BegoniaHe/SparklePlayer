#!/bin/bash
# 依赖更新恢复脚本
# 自动生成于 2025-06-09 10:13:20

echo "🔄 开始恢复依赖..."

# 恢复 Gradle 配置
echo "📁 恢复 Gradle 配置..."
cp "gradle/backups/build.gradle.kts.20250609_101310" build.gradle.kts
echo "✅ Gradle 配置已恢复"

# 恢复本地 JAR 文件
echo "📦 恢复本地 JAR 文件..."


# 清理临时文件
echo "🧹 清理临时文件..."
rm -f temp_*.jar

echo "✅ 恢复完成！"
echo "💡 现在可以运行 './gradlew clean build' 测试项目"
