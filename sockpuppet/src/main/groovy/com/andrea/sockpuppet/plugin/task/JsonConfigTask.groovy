package com.andrea.sockpuppet.plugin.task

import com.andrea.sockpuppet.plugin.data.Config
import com.andrea.sockpuppet.plugin.utils.JsonHelper
import com.andrea.sockpuppet.plugin.utils.StringUtils
import groovy.json.JsonSlurper
import org.apache.http.util.TextUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternSet

/**
 * 专门处理json配置
 */
class JsonConfigTask extends BaseConfigTask {

    @Override
    void injectProject(Project project) {
        super.injectProject(project)
        if (!(new File(mProject.sockpuppet.json.templatePath).exists())) {
            throw new GradleException("${mProject.sockpuppet.json.templatePath} not exist")
        } else {
            if (TextUtils.isEmpty(mProject.sockpuppet.json.sourcePath)) {
                throw new GradleException("${mProject.sockpuppet.json.sourcePath} not exist")
            }
        }
    }

    @Override
    void apply() {
        //获取各module
        mProject.getRootProject().getSubprojects().eachWithIndex { Project entry, int i ->
            def fileTree = entry.layout.projectDirectory.asFileTree
            parse(fileTree)
        }
    }

    @Override
    void parse(FileTree sourceFileTree) {
        def patternSet = new PatternSet();
        patternSet.include(mProject.sockpuppet.json.sourcePath)

        def resultFile = sourceFileTree.matching(patternSet)
        resultFile.eachWithIndex { File sourceFile, int sourceIndex ->
            if (isJsonMatch(sourceFile)) {
                def jsonSlurper = new JsonSlurper()
                def result = jsonSlurper.parse(sourceFile)
                if (result instanceof ArrayList) {
                    result.each { it ->
//                        println "build string ${it}------->"
                        if (it[Config.JSON_TYPE] == Config.KEY_PLACEHOLDER) {
                            parsePlaceholder(it[Config.JSON_LIST])
                        } else if (it[Config.JSON_TYPE] == Config.KEY_BUILD_CONFIG) {
                            parseBuildConfig(it[Config.JSON_LIST])
                        }
                    }
                }
            }
        }
    }

    /**
     * xml内容转换成BuildConfig
     * 会对boolean int string 做区分，转换成对应的类型
     * @param sourceFileTree
     */
    void parseBuildConfig(List list) {
        list.each { item ->
            def name = item[Config.JSON_NAME]
            def jsonFuncName = item[Config.JSON_FUNC_NAME]
            def value = item[Config.JSON_VALUE]
            if (StringUtils.isEmpty(jsonFuncName)) {//普通参数替换
                parseBuild(value)
            } else {//函数替换
                def result = getFuncResult(jsonFuncName, value as String[])
//                addBuildConfigString(name, result)
                parseBuild(result)
            }
        }
    }

    void parseBuild(Object value) {
        if (value instanceof ArrayList) {
            addBuildConfigList(name, value as String[])
        } else {
            if (StringUtils.isBool(value)) {//是不是bool值
                addBuildConfigBool(name, value)
            } else if (StringUtils.isInt(value)) {//是不是bool值
                addBuildConfigInt(name, value)
            } else {
                addBuildConfigString(name, value)
            }
        }
    }

    /**
     * xml内容转换成placeholder
     * 只支持string转换成placeholder，不支持string-array
     * @param sourceFileTree
     */
    void parsePlaceholder(List list) {
        list.each { item ->
            def name = item[Config.JSON_NAME]
            def jsonFuncName = item[Config.JSON_FUNC_NAME]
            def value = item[Config.JSON_VALUE]
            if (StringUtils.isEmpty(jsonFuncName)) {//普通参数替换
                addPlaceholder(name, value)
            } else {//函数替换
                def result = getFuncResult(jsonFuncName, value as String[])
                addPlaceholder(name, result)
            }
        }
    }

    /**
     * json文件配置和规定模版是不是一致
     * @param sourceFile
     * @return
     */
    boolean isJsonMatch(File sourceFile) {
        def templateFile = new File(mProject.sockpuppet.json.templatePath)
//        println("templateFile: " + templateFile.absolutePath)
        return JsonHelper.isMatchTemplate(templateFile, sourceFile)
    }

}