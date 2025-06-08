#!/bin/bash

# SparklePlayer .class文件清理脚本
# 清理项目中所有的.class文件

echo "开始清理 SparklePlayer 项目中的 .class 文件..."
echo "========================================"

# 统计清理前的.class文件数量
before_count=$(find . -name "*.class" -type f | wc -l)
echo "清理前 .class 文件数量: $before_count"

if [ $before_count -eq 0 ]; then
    echo "项目中没有找到 .class 文件，无需清理。"
    exit 0
fi

echo ""
echo "正在清理以下目录中的 .class 文件:"

# 清理 src 目录中的 .class 文件
if [ -d "src" ]; then
    src_count=$(find src -name "*.class" -type f | wc -l)
    if [ $src_count -gt 0 ]; then
        echo "- src/ 目录: $src_count 个文件"
        find src -name "*.class" -type f -delete
    fi
fi

# 清理 build 目录中的 .class 文件
if [ -d "build" ]; then
    build_count=$(find build -name "*.class" -type f | wc -l)
    if [ $build_count -gt 0 ]; then
        echo "- build/ 目录: $build_count 个文件"
        find build -name "*.class" -type f -delete
    fi
fi

# 清理 bin 目录中的 .class 文件
if [ -d "bin" ]; then
    bin_count=$(find bin -name "*.class" -type f | wc -l)
    if [ $bin_count -gt 0 ]; then
        echo "- bin/ 目录: $bin_count 个文件"
        find bin -name "*.class" -type f -delete
    fi
fi

# 清理其他可能的位置（以防万一）
other_count=$(find . -name "*.class" -type f -not -path "./src/*" -not -path "./build/*" -not -path "./bin/*" | wc -l)
if [ $other_count -gt 0 ]; then
    echo "- 其他位置: $other_count 个文件"
    find . -name "*.class" -type f -not -path "./src/*" -not -path "./build/*" -not -path "./bin/*" -delete
fi

echo ""

# 统计清理后的.class文件数量
after_count=$(find . -name "*.class" -type f | wc -l)
cleaned_count=$((before_count - after_count))

echo "========================================"
echo "清理完成！"
echo "已删除 $cleaned_count 个 .class 文件"
echo "剩余 .class 文件数量: $after_count"

if [ $after_count -eq 0 ]; then
    echo "✅ 所有 .class 文件已成功清理"
else
    echo "⚠️  仍有 $after_count 个 .class 文件未清理"
    echo "未清理的文件位置:"
    find . -name "*.class" -type f
fi

echo ""
echo "建议: 使用 './gradlew clean' 命令可以清理 Gradle 构建产生的文件"
