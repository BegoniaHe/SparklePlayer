#!/bin/bash

# SparklePlayer打包脚本
# 将当前目录打包成zip文件，命名为SparklePlayer+时间戳+.zip，保存在上级目录

# 获取当前Unix时间戳
timestamp=$(date +%s)

# 定义输出文件名
output_file="SparklePlayer_${timestamp}.zip"

# 获取上级目录路径
parent_dir=$(dirname "$PWD")

# 输出路径
output_path="${parent_dir}/${output_file}"

echo "开始打包 SparklePlayer 项目..."
echo "当前目录: $PWD"
echo "输出文件: $output_path"

# 切换到上级目录进行打包（这样zip文件中会包含SparklePlayer目录）
cd "$parent_dir"

# 创建zip文件，排除一些不必要的文件和目录
zip -r "$output_file" "SparklePlayer" \
    -x "SparklePlayer/build/*" \
    -x "SparklePlayer/.gradle/*" \
    -x "SparklePlayer/.DS_Store" \
    -x "SparklePlayer/bin/*" \
    -x "SparklePlayer/derby.log" \
    -x "SparklePlayer/.vscode/*" \
    -x "SparklePlayer/c:/*" \
    -x "SparklePlayer/.*"

# 检查打包是否成功
if [ $? -eq 0 ]; then
    echo "打包成功！"
    echo "文件已保存至: $output_path"
    echo "文件大小: $(du -h "$output_path" | cut -f1)"
else
    echo "打包失败！"
    exit 1
fi

# 返回原目录
cd -
