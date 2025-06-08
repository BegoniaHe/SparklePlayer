#!/bin/bash

echo "Java代码统计报告"
echo "=================="

# 统计文件数量
file_count=$(find . -type f -name "*.java" | wc -l)
echo "Java文件数量: $file_count"

if [ $file_count -eq 0 ]; then
    echo "当前目录及子目录中没有找到Java文件"
    exit 0
fi

# 统计总行数
total_lines=$(find . -type f -name "*.java" -exec wc -l {} + | tail -1 | awk '{print $1}')
echo "总行数: $total_lines"

# 计算平均行数
if [ $file_count -gt 0 ]; then
    average_lines=$((total_lines / file_count))
    echo "平均每个文件行数: $average_lines"
fi

echo "=================="
