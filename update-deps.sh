#!/bin/bash

# 一键依赖更新脚本
# 提供交互式菜单来选择更新选项

set -e

echo "🚀 SparklePlayer 依赖管理工具"
echo "====================================="
echo ""
echo "请选择操作："
echo "1) 预览可更新的依赖 (推荐先执行)"
echo "2) 执行依赖更新"
echo "3) 仅显示现代化建议"
echo "4) 退出"
echo ""

read -p "请输入选项 (1-4): " choice

case $choice in
    1)
        echo ""
        echo "📋 预览可更新的依赖..."
        python3 update-dependencies.py --dry-run
        ;;
    2)
        echo ""
        echo "⚠️  即将更新依赖，这将修改 build.gradle.kts 文件"
        read -p "确认继续？(y/N): " confirm
        
        if [[ $confirm == [yY] || $confirm == [yY][eE][sS] ]]; then
            echo ""
            echo "🔄 正在更新依赖..."
            python3 update-dependencies.py
            
            echo ""
            echo "✅ 更新完成！建议运行以下命令测试："
            echo "   ./gradlew clean build"
        else
            echo "❌ 已取消更新"
        fi
        ;;
    3)
        echo ""
        python3 update-dependencies.py --suggestions
        ;;
    4)
        echo "👋 再见！"
        exit 0
        ;;
    *)
        echo "❌ 无效选项，请重新运行脚本"
        exit 1
        ;;
esac

echo ""
echo "📖 更多信息请查看 DEPENDENCY_UPDATE_README.md"
