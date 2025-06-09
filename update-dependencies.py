#!/usr/bin/env python3
"""
SparklePlayer 依赖更新工具
用于将Maven仓库中的依赖更新到最新版本
"""

import json
import re
import shutil
import subprocess
import sys
import urllib.request
import urllib.parse
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Optional, Tuple

class DependencyUpdater:
    def __init__(self):
        self.build_file = Path("build.gradle.kts")
        self.backup_dir = Path("gradle/backups")
        self.libs_dir = Path("sparkleplayer/libs")
        self.musique_dir = Path("sparkleplayer/libs/musique")
        self.backup_dir.mkdir(parents=True, exist_ok=True)
        
        # 定义需要检查的依赖 (版本将从gradle文件中动态读取)
        self.dependency_patterns = [
            ("commons-beanutils", "commons-beanutils"),
            ("commons-codec", "commons-codec"),
            ("commons-collections", "commons-collections"),
            ("commons-lang", "commons-lang"),
            ("commons-logging", "commons-logging"),
            ("net.sf.ezmorph", "ezmorph"),
            ("net.sf.json-lib", "json-lib"),
            ("log4j", "log4j"),
            ("org.apache.derby", "derby"),
            ("com.h3xstream.findsecbugs", "findsecbugs-plugin"),
        ]
        
        # 音频处理库依赖 (在Maven Central上可能有更新版本)
        self.audio_dependencies = [
            ("org.jaudiotagger", "jaudiotagger", "3.0.1"),  # 当前本地版本可能是旧版本号
            ("javazoom", "jlayer", "1.0.1"),
            ("org.jflac", "jflac", "1.3"),
            ("com.googlecode.soundlibs", "jorbis", "0.0.17.4"),
        ]
        
        # 现代化替换建议
        self.modernization_suggestions = {
            "commons-lang:commons-lang": "org.apache.commons:commons-lang3",
            "commons-collections:commons-collections": "org.apache.commons:commons-collections4", 
            "log4j:log4j": "org.apache.logging.log4j:log4j-core",
            "net.sf.json-lib:json-lib": "com.fasterxml.jackson.core:jackson-databind",
        }

    def get_latest_version(self, group_id: str, artifact_id: str) -> Optional[str]:
        """从Maven Central获取最新版本"""
        try:
            # 构建搜索URL
            query = f'g:"{group_id}" AND a:"{artifact_id}"'
            encoded_query = urllib.parse.quote(query)
            url = f"https://search.maven.org/solrsearch/select?q={encoded_query}&core=gav&rows=1&wt=json"
            
            print(f"🔍 检查 {group_id}:{artifact_id} ...")
            
            with urllib.request.urlopen(url, timeout=10) as response:
                data = json.loads(response.read().decode())
                
            docs = data.get("response", {}).get("docs", [])
            if docs:
                version = docs[0].get("v")
                print(f"   最新版本: {version}")
                return version
            else:
                print(f"   ⚠️  未找到版本信息")
                return None
                
        except Exception as e:
            print(f"   ❌ 获取版本失败: {e}")
            return None

    def backup_build_file(self) -> Path:
        """备份构建文件"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        backup_path = self.backup_dir / f"build.gradle.kts.{timestamp}"
        shutil.copy2(self.build_file, backup_path)
        print(f"📁 已备份到: {backup_path}")
        return backup_path

    def update_dependency_in_content(self, content: str, group_id: str, artifact_id: str, 
                                   old_version: str, new_version: str) -> str:
        """在文件内容中更新依赖版本"""
        patterns = [
            # 标准格式: implementation("group:artifact:version")
            f'("{re.escape(group_id)}:{re.escape(artifact_id)}:{re.escape(old_version)}")',
            # 带分类器: implementation("group:artifact:version:classifier")
            f'("{re.escape(group_id)}:{re.escape(artifact_id)}:{re.escape(old_version)}:([^"]*)")',
        ]
        
        updated = False
        for pattern in patterns:
            old_pattern = re.compile(pattern)
            if old_pattern.search(content):
                if ":jdk15" in content and artifact_id == "json-lib":
                    # 特殊处理json-lib的jdk15分类器
                    replacement = f'("{group_id}:{artifact_id}:{new_version}:jdk15")'
                    content = re.sub(
                        f'("{re.escape(group_id)}:{re.escape(artifact_id)}:{re.escape(old_version)}:jdk15")',
                        replacement,
                        content
                    )
                else:
                    replacement = f'("{group_id}:{artifact_id}:{new_version}")'
                    content = re.sub(pattern, replacement, content)
                updated = True
                break
        
        return content, updated

    def extract_current_versions(self) -> List[Tuple[str, str, str]]:
        """从gradle文件中提取当前依赖版本"""
        if not self.build_file.exists():
            return []
        
        content = self.build_file.read_text(encoding='utf-8')
        dependencies = []
        
        for group_id, artifact_id in self.dependency_patterns:
            # 匹配各种可能的依赖声明格式
            patterns = [
                rf'implementation\(\("({re.escape(group_id)}):({re.escape(artifact_id)}):([^"]+)"\)\)',
                rf'implementation\("({re.escape(group_id)}):({re.escape(artifact_id)}):([^"]+)"\)',
                rf'spotbugsPlugins\(\("({re.escape(group_id)}):({re.escape(artifact_id)}):([^"]+)"\)\)',
                rf'spotbugsPlugins\("({re.escape(group_id)}):({re.escape(artifact_id)}):([^"]+)"\)',
            ]
            
            for pattern in patterns:
                matches = re.search(pattern, content)
                if matches:
                    found_group = matches.group(1)
                    found_artifact = matches.group(2)
                    found_version = matches.group(3)
                    # 处理特殊情况如json-lib:2.4:jdk15
                    if ":jdk15" in found_version:
                        found_version = found_version.replace(":jdk15", "")
                    dependencies.append((found_group, found_artifact, found_version))
                    break
        
        return dependencies

    def update_dependencies(self, dry_run: bool = False) -> Dict[str, Tuple[str, str]]:
        """更新所有依赖"""
        print("🔄 开始检查依赖更新...")
        
        if not self.build_file.exists():
            print(f"❌ 构建文件不存在: {self.build_file}")
            return {}
        
        # 动态提取当前版本
        current_dependencies = self.extract_current_versions()
        if not current_dependencies:
            print("❌ 无法从Gradle文件中提取依赖信息")
            return {}
        
        # 读取当前文件内容
        content = self.build_file.read_text(encoding='utf-8')
        updated_content = content
        gradle_updates = {}
        
        # 1. 更新Gradle配置中的依赖版本
        print("\n📋 检查Gradle配置更新...")
        for group_id, artifact_id, current_version in current_dependencies:
            latest_version = self.get_latest_version(group_id, artifact_id)
            
            if latest_version:
                version_cmp = self.compare_versions(current_version, latest_version)
                if version_cmp < 0:  # current < latest
                    print(f"📦 Gradle: {group_id}:{artifact_id}: {current_version} -> {latest_version}")
                    gradle_updates[f"{group_id}:{artifact_id}"] = (current_version, latest_version)
                    
                    if not dry_run:
                        updated_content, was_updated = self.update_dependency_in_content(
                            updated_content, group_id, artifact_id, current_version, latest_version
                        )
                        if not was_updated:
                            print(f"   ⚠️  未能在文件中找到对应的依赖声明")
                elif version_cmp == 0:  # current == latest
                    print(f"✅ Gradle: {group_id}:{artifact_id}: 已是最新版本 {current_version}")
                else:  # current > latest (unusual case)
                    print(f"🔍 Gradle: {group_id}:{artifact_id}: 当前版本 {current_version} 高于Maven版本 {latest_version}")
            else:
                print(f"⚠️  Gradle: {group_id}:{artifact_id}: 无法获取最新版本，保持 {current_version}")
        
        # 2. 更新本地JAR文件
        print("\n📂 检查本地JAR文件更新...")
        local_updates = self.update_local_libraries(dry_run)
        
        # 3. 合并更新结果
        all_updates = {**gradle_updates, **local_updates}
        
        if gradle_updates and not dry_run:
            # 写入更新后的Gradle配置
            self.build_file.write_text(updated_content, encoding='utf-8')
            print(f"✅ Gradle: 已更新 {len(gradle_updates)} 个依赖配置")
        
        if local_updates and not dry_run:
            # 更新Gradle配置中的本地JAR引用
            if gradle_updates or local_updates:
                content = self.build_file.read_text(encoding='utf-8')
                updated_content = self.update_gradle_local_jar_references(content, local_updates)
                self.build_file.write_text(updated_content, encoding='utf-8')
            
            print(f"✅ 本地库: 已更新 {len(local_updates)} 个JAR文件")
        
        return all_updates

    def suggest_modernizations(self):
        """提供现代化建议"""
        print("\n💡 依赖现代化建议:")
        print("="*50)
        
        suggestions = [
            ("commons-lang:commons-lang", "org.apache.commons:commons-lang3", 
             "更好的API设计和性能"),
            ("commons-collections:commons-collections", "org.apache.commons:commons-collections4",
             "类型安全和现代Java特性"),
            ("log4j:log4j", "org.apache.logging.log4j:log4j-core", 
             "安全性修复和更好的性能"),
            ("net.sf.json-lib:json-lib", "com.fasterxml.jackson.core:jackson-databind",
             "更快的解析速度和更好的维护"),
        ]
        
        for old_dep, new_dep, reason in suggestions:
            print(f"• {old_dep}")
            print(f"  -> {new_dep}")
            print(f"     理由: {reason}")
            print()

    def check_gradle_wrapper(self):
        """检查Gradle wrapper"""
        gradlew = Path("./gradlew")
        if not gradlew.exists():
            print("❌ 未找到 gradlew，请确保在项目根目录运行")
            return False
        return True

    def test_build(self):
        """测试构建"""
        print("🔧 测试构建配置...")
        try:
            result = subprocess.run(
                ["./gradlew", "dependencies", "--configuration", "compileClasspath"],
                capture_output=True, text=True, timeout=60
            )
            if result.returncode == 0:
                print("✅ 依赖解析成功")
                return True
            else:
                print("❌ 依赖解析失败:")
                print(result.stderr)
                return False
        except subprocess.TimeoutExpired:
            print("⏰ 构建测试超时")
            return False
        except Exception as e:
            print(f"❌ 构建测试出错: {e}")
            return False

    def clean_build(self):
        """清理构建"""
        print("🧹 清理构建缓存...")
        try:
            subprocess.run(["./gradlew", "clean"], check=True, capture_output=True)
            print("✅ 清理完成")
        except subprocess.CalledProcessError:
            print("⚠️  清理失败，但继续进行")

    def download_jar(self, group_id: str, artifact_id: str, version: str, 
                    classifier: str = None) -> Optional[Path]:
        """从Maven Central下载JAR文件"""
        try:
            # 构建下载URL
            group_path = group_id.replace(".", "/")
            filename = f"{artifact_id}-{version}"
            if classifier:
                filename += f"-{classifier}"
            filename += ".jar"
            
            url = f"https://repo1.maven.org/maven2/{group_path}/{artifact_id}/{version}/{filename}"
            
            # 创建临时下载路径
            temp_path = Path(f"temp_{filename}")
            
            print(f"📥 下载 {filename}...")
            print(f"   URL: {url}")
            
            # 下载文件并显示进度
            def show_progress(block_num, block_size, total_size):
                if total_size > 0:
                    percent = min(100, (block_num * block_size * 100) / total_size)
                    print(f"\r   进度: {percent:.1f}%", end="", flush=True)
            
            urllib.request.urlretrieve(url, temp_path, show_progress)
            print()  # 换行
            
            # 验证下载的文件
            if temp_path.exists() and temp_path.stat().st_size > 0:
                if self.verify_jar_integrity(temp_path):
                    print(f"✅ 下载完成: {filename}")
                    return temp_path
                else:
                    print(f"❌ 文件损坏: {filename}")
                    temp_path.unlink()
                    return None
            else:
                print(f"❌ 下载失败: {filename}")
                return None
            
        except Exception as e:
            print(f"❌ 下载失败: {e}")
            return None

    def update_local_jar(self, group_id: str, artifact_id: str, old_version: str, 
                        new_version: str, target_dir: Path, dry_run: bool = False) -> bool:
        """更新本地JAR文件"""
        try:
            # 查找旧的JAR文件
            old_jar_patterns = [
                f"{artifact_id}-{old_version}.jar",
                f"{artifact_id}-{old_version}-jdk15.jar",  # 处理json-lib特殊情况
                f"{artifact_id}.jar",  # 没有版本号的情况
            ]
            
            old_jar_path = None
            for pattern in old_jar_patterns:
                potential_path = target_dir / pattern
                if potential_path.exists():
                    old_jar_path = potential_path
                    break
            
            if not old_jar_path:
                print(f"⚠️  未找到旧JAR文件: {artifact_id}")
                return False
            
            if dry_run:
                new_filename = f"{artifact_id}-{new_version}"
                if artifact_id == "json-lib":
                    new_filename += "-jdk15"
                new_filename += ".jar"
                print(f"📦 将更新本地JAR: {old_jar_path.name} -> {new_filename}")
                return True
            
            # 下载新版本
            classifier = "jdk15" if artifact_id == "json-lib" else None
            temp_jar = self.download_jar(group_id, artifact_id, new_version, classifier)
            
            if not temp_jar:
                return False
            
            # 备份旧文件
            backup_path = target_dir / f"{old_jar_path.name}.backup"
            shutil.copy2(old_jar_path, backup_path)
            print(f"💾 已备份: {backup_path}")
            
            # 替换新文件
            new_filename = f"{artifact_id}-{new_version}"
            if artifact_id == "json-lib":
                new_filename += "-jdk15"
            new_filename += ".jar"
            new_jar_path = target_dir / new_filename
            shutil.move(temp_jar, new_jar_path)
            
            # 删除旧文件（如果文件名不同）
            if old_jar_path != new_jar_path:
                old_jar_path.unlink()
                print(f"🗑️  已删除旧文件: {old_jar_path.name}")
            
            print(f"✅ 已更新: {new_jar_path.name}")
            return True
            
        except Exception as e:
            print(f"❌ 更新本地JAR失败: {e}")
            return False

    def extract_local_jar_versions(self) -> Dict[str, str]:
        """从本地JAR文件名中提取版本信息"""
        local_versions = {}
        
        # 定义本地库文件映射
        local_patterns = [
            # (Maven groupId, artifactId, local_dir, local_filename_pattern)
            ("commons-beanutils", "commons-beanutils", self.libs_dir, "commons-beanutils-*.jar"),
            ("commons-codec", "commons-codec", self.libs_dir, "commons-codec-*.jar"),
            ("commons-collections", "commons-collections", self.libs_dir, "commons-collections-*.jar"),
            ("commons-lang", "commons-lang", self.libs_dir, "commons-lang-*.jar"),
            ("commons-logging", "commons-logging", self.libs_dir, "commons-logging-*.jar"),
            ("net.sf.ezmorph", "ezmorph", self.libs_dir, "ezmorph-*.jar"),
            ("net.sf.json-lib", "json-lib", self.libs_dir, "json-lib-*-jdk15.jar"),
            ("log4j", "log4j", self.libs_dir, "log4j-*.jar"),
            ("org.apache.derby", "derby", self.libs_dir, "derby-*.jar"),
            ("org.jaudiotagger", "jaudiotagger", self.musique_dir, "jaudiotagger-*.jar"),
        ]
        
        for group_id, artifact_id, target_dir, pattern in local_patterns:
            try:
                # 查找匹配的JAR文件
                import glob
                search_pattern = str(target_dir / pattern)
                matching_files = glob.glob(search_pattern)
                
                if matching_files:
                    # 取第一个匹配文件并从文件名提取版本
                    jar_file = Path(matching_files[0])
                    filename = jar_file.stem  # 去掉.jar扩展名
                    
                    # 根据不同库的命名规则提取版本
                    if artifact_id == "json-lib":
                        # json-lib-2.4-jdk15.jar -> 2.4
                        match = re.search(rf'{re.escape(artifact_id)}-([^-]+)-jdk15', filename)
                        if match:
                            local_versions[f"{group_id}:{artifact_id}"] = match.group(1)
                    else:
                        # 标准格式: artifact-version.jar -> version
                        match = re.search(rf'{re.escape(artifact_id)}-(.+)', filename)
                        if match:
                            local_versions[f"{group_id}:{artifact_id}"] = match.group(1)
            except Exception as e:
                print(f"⚠️  提取本地版本失败 {artifact_id}: {e}")
        
        return local_versions

    def update_local_libraries(self, dry_run: bool = False) -> Dict[str, Tuple[str, str]]:
        """更新本地库文件"""
        print("🔄 检查本地库更新...")
        
        # 动态获取本地JAR文件版本
        local_versions = self.extract_local_jar_versions()
        
        # 定义本地库和对应的Maven依赖映射（不再包含硬编码版本）
        local_lib_mappings = [
            # (Maven groupId, artifactId, local_dir, local_filename_pattern)
            ("commons-beanutils", "commons-beanutils", self.libs_dir, "commons-beanutils-*.jar"),
            ("commons-codec", "commons-codec", self.libs_dir, "commons-codec-*.jar"),
            ("commons-collections", "commons-collections", self.libs_dir, "commons-collections-*.jar"),
            ("commons-lang", "commons-lang", self.libs_dir, "commons-lang-*.jar"),
            ("commons-logging", "commons-logging", self.libs_dir, "commons-logging-*.jar"),
            ("net.sf.ezmorph", "ezmorph", self.libs_dir, "ezmorph-*.jar"),
            ("net.sf.json-lib", "json-lib", self.libs_dir, "json-lib-*-jdk15.jar"),
            ("log4j", "log4j", self.libs_dir, "log4j-*.jar"),
            ("org.apache.derby", "derby", self.libs_dir, "derby-*.jar"),
            # 音频处理库
            ("org.jaudiotagger", "jaudiotagger", self.musique_dir, "jaudiotagger-*.jar"),
        ]
        
        updates = {}
        
        for group_id, artifact_id, target_dir, pattern in local_lib_mappings:
            # 从动态提取的版本中获取当前版本
            local_key = f"{group_id}:{artifact_id}"
            current_version = local_versions.get(local_key)
            
            if not current_version:
                print(f"⚠️  未找到本地JAR文件: {artifact_id}")
                continue
                
            latest_version = self.get_latest_version(group_id, artifact_id)
            
            if latest_version:
                version_cmp = self.compare_versions(current_version, latest_version)
                if version_cmp < 0:  # current < latest
                    print(f"📦 本地库 {artifact_id}: {current_version} -> {latest_version}")
                    updates[f"{group_id}:{artifact_id}"] = (current_version, latest_version)
                    
                    if not dry_run:
                        success = self.update_local_jar(
                            group_id, artifact_id, current_version, latest_version, target_dir, dry_run
                        )
                        if not success:
                            print(f"⚠️  {artifact_id} 本地更新失败")
                elif version_cmp == 0:  # current == latest
                    print(f"✅ 本地库 {artifact_id}: 已是最新版本 {current_version}")
                else:  # current > latest (unusual case)
                    print(f"🔍 本地库 {artifact_id}: 本地版本 {current_version} 高于Maven版本 {latest_version}")
            else:
                print(f"⚠️  本地库 {artifact_id}: 无法获取最新版本信息")
        
        return updates

    def update_gradle_local_jar_references(self, content: str, updates: Dict[str, Tuple[str, str]]) -> str:
        """更新Gradle配置中本地JAR文件的引用"""
        updated_content = content
        
        for dep_key, (old_version, new_version) in updates.items():
            group_id, artifact_id = dep_key.split(":", 1)
            
            # 更新本地JAR的名称引用
            patterns = [
                # 匹配 implementation(group = "", name = "artifact-version", ...)
                rf'(name = ")({re.escape(artifact_id)}-{re.escape(old_version)})(")',
                # 匹配其他可能的格式
                rf'("{re.escape(artifact_id)}-{re.escape(old_version)}\.jar")',
            ]
            
            for pattern in patterns:
                old_pattern = re.compile(pattern)
                if old_pattern.search(updated_content):
                    replacement = rf'\g<1>{artifact_id}-{new_version}\g<3>'
                    updated_content = old_pattern.sub(replacement, updated_content)
                    print(f"🔧 已更新Gradle中的JAR引用: {artifact_id}-{old_version} -> {artifact_id}-{new_version}")
                    break
        
        return updated_content

    def cleanup_temp_files(self):
        """清理临时文件"""
        temp_files = list(Path(".").glob("temp_*.jar"))
        for temp_file in temp_files:
            try:
                temp_file.unlink()
                print(f"🧹 已清理临时文件: {temp_file}")
            except Exception as e:
                print(f"⚠️  清理临时文件失败: {e}")

    def verify_jar_integrity(self, jar_path: Path) -> bool:
        """验证JAR文件完整性"""
        try:
            import zipfile
            with zipfile.ZipFile(jar_path, 'r') as zip_file:
                # 尝试读取JAR文件的清单
                zip_file.testzip()
                return True
        except Exception as e:
            print(f"❌ JAR文件验证失败 {jar_path}: {e}")
            return False

    def create_recovery_script(self, backup_path: Path, updated_jars: List[str]):
        """创建恢复脚本"""
        recovery_script = Path("recover-dependencies.sh")
        
        script_content = f"""#!/bin/bash
# 依赖更新恢复脚本
# 自动生成于 {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}

echo "🔄 开始恢复依赖..."

# 恢复 Gradle 配置
echo "📁 恢复 Gradle 配置..."
cp "{backup_path}" build.gradle.kts
echo "✅ Gradle 配置已恢复"

# 恢复本地 JAR 文件
echo "📦 恢复本地 JAR 文件..."
"""
        
        for jar_name in updated_jars:
            script_content += f"""
if [ -f "sparkleplayer/libs/{jar_name}.backup" ]; then
    mv "sparkleplayer/libs/{jar_name}.backup" "sparkleplayer/libs/{jar_name}"
    echo "✅ 已恢复: {jar_name}"
fi
if [ -f "sparkleplayer/libs/musique/{jar_name}.backup" ]; then
    mv "sparkleplayer/libs/musique/{jar_name}.backup" "sparkleplayer/libs/musique/{jar_name}"
    echo "✅ 已恢复: musique/{jar_name}"
fi"""
        
        script_content += """

# 清理临时文件
echo "🧹 清理临时文件..."
rm -f temp_*.jar

echo "✅ 恢复完成！"
echo "💡 现在可以运行 './gradlew clean build' 测试项目"
"""
        
        recovery_script.write_text(script_content)
        recovery_script.chmod(0o755)
        
        print(f"📋 已创建恢复脚本: {recovery_script}")
        return recovery_script

    def compare_versions(self, version1: str, version2: str) -> int:
        """比较两个版本号
        返回值: -1 (version1 < version2), 0 (相等), 1 (version1 > version2)
        """
        try:
            def normalize_version(v):
                # 移除非数字字符（如beta, alpha等）
                import re
                parts = re.findall(r'\d+', v)
                return [int(x) for x in parts] if parts else [0]
            
            v1_parts = normalize_version(version1)
            v2_parts = normalize_version(version2)
            
            # 填充到相同长度
            max_len = max(len(v1_parts), len(v2_parts))
            v1_parts.extend([0] * (max_len - len(v1_parts)))
            v2_parts.extend([0] * (max_len - len(v2_parts)))
            
            for i in range(max_len):
                if v1_parts[i] < v2_parts[i]:
                    return -1
                elif v1_parts[i] > v2_parts[i]:
                    return 1
            
            return 0
        except Exception:
            # 如果比较失败，采用字符串比较
            if version1 < version2:
                return -1
            elif version1 > version2:
                return 1
            else:
                return 0

def main():
    import argparse
    
    parser = argparse.ArgumentParser(description="更新SparklePlayer项目的Maven依赖")
    parser.add_argument("--dry-run", action="store_true", help="仅显示将要更新的内容")
    parser.add_argument("--no-test", action="store_true", help="跳过构建测试")
    parser.add_argument("--suggestions", action="store_true", help="仅显示现代化建议")
    
    args = parser.parse_args()
    
    updater = DependencyUpdater()
    
    print("="*60)
    print("   SparklePlayer 依赖更新工具")
    print("="*60)
    print()
    
    if args.suggestions:
        updater.suggest_modernizations()
        return
    
    if not updater.check_gradle_wrapper():
        sys.exit(1)
    
    if not args.dry_run:
        backup_path = updater.backup_build_file()
    
    updates = updater.update_dependencies(dry_run=args.dry_run)
    
    if args.dry_run:
        print(f"\n🔍 干运行完成，发现 {len(updates)} 个可更新的依赖")
        if updates:
            print("\n可更新的依赖:")
            gradle_count = 0
            local_count = 0
            for dep, (old, new) in updates.items():
                # 判断是Gradle依赖还是本地库
                is_local = any(dep.endswith(f":{lib}") for lib in ["jaudiotagger"])
                if is_local:
                    print(f"  • [本地JAR] {dep}: {old} -> {new}")
                    local_count += 1
                else:
                    print(f"  • [Gradle] {dep}: {old} -> {new}")
                    gradle_count += 1
            print(f"\n📊 统计: Gradle依赖 {gradle_count} 个, 本地JAR {local_count} 个")
    else:
        if updates:
            gradle_count = sum(1 for dep in updates if not any(dep.endswith(f":{lib}") for lib in ["jaudiotagger"]))
            local_count = len(updates) - gradle_count
            
            print(f"\n✅ 已更新 {len(updates)} 个依赖 (Gradle: {gradle_count}, 本地JAR: {local_count})")
            
            # 创建恢复脚本
            updated_jars = [dep.split(":")[-1] for dep in updates if any(dep.endswith(f":{lib}") for lib in ["jaudiotagger"])]
            recovery_script = updater.create_recovery_script(backup_path, updated_jars)
            
            # 清理临时文件
            updater.cleanup_temp_files()
            
            if not args.no_test:
                updater.clean_build()
                if not updater.test_build():
                    print(f"\n❌ 构建测试失败，可以使用恢复脚本:")
                    print(f"   ./{recovery_script}")
                    sys.exit(1)
            
            print("\n🎉 依赖更新完成！")
            print("\n📋 后续步骤:")
            print("1. 运行 './gradlew build' 完整测试")
            print("2. 测试应用程序功能") 
            print("3. 如有问题，运行恢复脚本:")
            print(f"   ./{recovery_script}")
            print("   - 或手动恢复:")
            print("     - Gradle配置备份: gradle/backups/")
            print("     - JAR文件备份: 在libs目录中查找.backup文件")
        else:
            print("\n✅ 所有依赖都已是最新版本")
    
    print()
    updater.suggest_modernizations()

if __name__ == "__main__":
    main()
