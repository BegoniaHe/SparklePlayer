#!/bin/bash

# SparklePlayer 依赖更新脚本
# 用于将Maven仓库中的依赖更新到最新版本

set -e

echo "🚀 开始更新 SparklePlayer 项目依赖..."

# 检查是否有必要的工具
check_requirements() {
    echo "📋 检查必要工具..."
    
    if ! command -v curl &> /dev/null; then
        echo "❌ curl 未安装，请先安装 curl"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        echo "⚠️  建议安装 jq 工具以获得更好的JSON解析体验"
        echo "   安装命令: brew install jq"
    fi
    
    echo "✅ 工具检查完成"
}

# 获取Maven Central上的最新版本
get_latest_version() {
    local group_id="$1"
    local artifact_id="$2"
    
    # 使用Maven Central搜索API
    local url="https://search.maven.org/solrsearch/select?q=g:\"${group_id}\"+AND+a:\"${artifact_id}\"&core=gav&rows=1&wt=json"
    
    local response=$(curl -s "$url")
    
    if command -v jq &> /dev/null; then
        echo "$response" | jq -r '.response.docs[0].v // "unknown"'
    else
        # 简单的grep方式解析（如果没有jq）
        echo "$response" | grep -o '"v":"[^"]*"' | head -1 | cut -d'"' -f4
    fi
}

# 备份原文件
backup_build_file() {
    echo "📁 备份 build.gradle.kts..."
    cp build.gradle.kts "build.gradle.kts.backup.$(date +%Y%m%d_%H%M%S)"
    echo "✅ 备份完成"
}

# 更新依赖版本
update_dependencies() {
    echo "🔄 开始更新依赖版本..."
    
    # 定义需要更新的依赖项（groupId:artifactId:currentVersion）
    declare -a dependencies=(
        "commons-beanutils:commons-beanutils:1.7.0"
        "commons-codec:commons-codec:1.10"
        "commons-collections:commons-collections:3.1"
        "commons-lang:commons-lang:2.5"
        "commons-logging:commons-logging:1.2"
        "net.sf.ezmorph:ezmorph:1.0.3"
        "net.sf.json-lib:json-lib:2.1"
        "log4j:log4j:1.2.8"
        "org.apache.derby:derby:10.4.2.0"
        "com.h3xstream.findsecbugs:findsecbugs-plugin:1.12.0"
    )
    
    local temp_file="build.gradle.kts.tmp"
    cp build.gradle.kts "$temp_file"
    
    for dep in "${dependencies[@]}"; do
        IFS=':' read -r group_id artifact_id current_version <<< "$dep"
        
        echo "🔍 检查 ${group_id}:${artifact_id} 的最新版本..."
        
        latest_version=$(get_latest_version "$group_id" "$artifact_id")
        
        if [ "$latest_version" != "unknown" ] && [ "$latest_version" != "" ]; then
            if [ "$latest_version" != "$current_version" ]; then
                echo "📦 更新 ${group_id}:${artifact_id}: ${current_version} -> ${latest_version}"
                
                # 更新build.gradle.kts文件
                sed -i.bak "s|${group_id}:${artifact_id}:${current_version}|${group_id}:${artifact_id}:${latest_version}|g" "$temp_file"
                
                # 特殊处理json-lib的jdk15分类器
                if [ "$artifact_id" = "json-lib" ]; then
                    sed -i.bak "s|${group_id}:${artifact_id}:${latest_version}|${group_id}:${artifact_id}:${latest_version}:jdk15|g" "$temp_file"
                fi
            else
                echo "✅ ${group_id}:${artifact_id} 已是最新版本: ${current_version}"
            fi
        else
            echo "⚠️  无法获取 ${group_id}:${artifact_id} 的最新版本，保持当前版本: ${current_version}"
        fi
    done
    
    # 移动临时文件到正式文件
    mv "$temp_file" build.gradle.kts
    rm -f build.gradle.kts.tmp.bak
    
    echo "✅ 依赖版本更新完成"
}

# 处理过时的依赖建议
suggest_replacements() {
    echo "💡 依赖替换建议:"
    echo "   - commons-lang:commons-lang -> org.apache.commons:commons-lang3"
    echo "   - commons-collections:commons-collections -> org.apache.commons:commons-collections4"
    echo "   - log4j:log4j -> org.apache.logging.log4j:log4j-core (Log4j 2.x)"
    echo "   - net.sf.json-lib:json-lib -> com.fasterxml.jackson.core:jackson-databind (推荐)"
    echo ""
    echo "⚠️  注意: 某些依赖可能有API变化，更新后请测试应用程序功能"
}

# 显示本地JAR依赖信息
show_local_jars() {
    echo "📂 本地JAR依赖（无法自动更新）:"
    echo "   位置: sparkleplayer/libs/ 和 sparkleplayer/libs/musique/"
    echo "   这些依赖需要手动检查和更新:"
    echo "   - JPlayer-lib.jar"
    echo "   - TweenEngine-lib.jar"
    echo "   - 各种音频编解码器库"
    echo ""
    echo "💡 建议: 检查是否有这些库的Maven Central版本可用"
}

# 清理和测试
cleanup_and_test() {
    echo "🧹 清理构建缓存..."
    ./gradlew clean
    
    echo "🔧 测试依赖解析..."
    if ./gradlew dependencies --configuration compileClasspath > /dev/null 2>&1; then
        echo "✅ 依赖解析成功"
    else
        echo "❌ 依赖解析失败，请检查更新的依赖"
        echo "📁 可以从备份文件恢复: build.gradle.kts.backup.*"
        exit 1
    fi
}

# 生成依赖报告
generate_report() {
    echo "📊 生成依赖报告..."
    ./gradlew dependencyInsight --dependency commons-beanutils
    echo ""
    echo "📝 完整依赖报告已生成，可以运行以下命令查看:"
    echo "   ./gradlew dependencies"
    echo "   ./gradlew dependencyUpdates (需要gradle-versions-plugin)"
}

# 主函数
main() {
    echo "============================================"
    echo "   SparklePlayer 依赖更新脚本"
    echo "============================================"
    echo ""
    
    check_requirements
    backup_build_file
    update_dependencies
    suggest_replacements
    show_local_jars
    cleanup_and_test
    
    echo ""
    echo "🎉 依赖更新完成！"
    echo ""
    echo "📋 后续步骤:"
    echo "1. 运行 './gradlew build' 测试构建"
    echo "2. 运行应用程序测试功能"
    echo "3. 如果有问题，从备份文件恢复"
    echo ""
    echo "📁 备份文件: build.gradle.kts.backup.*"
    echo ""
}

# 处理命令行参数
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    echo "使用方法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  --help, -h     显示此帮助信息"
    echo "  --dry-run      仅显示将要更新的内容，不实际更新"
    echo ""
    echo "此脚本将："
    echo "1. 备份当前的 build.gradle.kts 文件"
    echo "2. 检查 Maven Central 上的最新版本"
    echo "3. 更新依赖版本"
    echo "4. 测试依赖解析"
    echo ""
    exit 0
fi

if [ "$1" = "--dry-run" ]; then
    echo "🔍 干运行模式 - 仅显示将要更新的内容"
    echo ""
    
    declare -a dependencies=(
        "commons-beanutils:commons-beanutils:1.7.0"
        "commons-codec:commons-codec:1.10"
        "commons-collections:commons-collections:3.1"
        "commons-lang:commons-lang:2.5"
        "commons-logging:commons-logging:1.2"
        "net.sf.ezmorph:ezmorph:1.0.3"
        "net.sf.json-lib:json-lib:2.1"
        "log4j:log4j:1.2.8"
        "org.apache.derby:derby:10.4.2.0"
        "com.h3xstream.findsecbugs:findsecbugs-plugin:1.12.0"
    )
    
    for dep in "${dependencies[@]}"; do
        IFS=':' read -r group_id artifact_id current_version <<< "$dep"
        latest_version=$(get_latest_version "$group_id" "$artifact_id")
        
        if [ "$latest_version" != "unknown" ] && [ "$latest_version" != "" ]; then
            if [ "$latest_version" != "$current_version" ]; then
                echo "📦 将更新: ${group_id}:${artifact_id}: ${current_version} -> ${latest_version}"
            else
                echo "✅ 已最新: ${group_id}:${artifact_id}: ${current_version}"
            fi
        else
            echo "⚠️  无法检查: ${group_id}:${artifact_id}: ${current_version}"
        fi
    done
    
    echo ""
    suggest_replacements
    exit 0
fi

# 运行主程序
main
