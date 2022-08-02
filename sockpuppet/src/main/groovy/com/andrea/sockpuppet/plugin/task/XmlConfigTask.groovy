package com.andrea.sockpuppet.plugin.task

import com.andrea.sockpuppet.plugin.utils.StringUtils
import com.andrea.sockpuppet.plugin.utils.XmlHelper
import org.apache.http.util.TextUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternSet

/**
 * 专门处理xml配置
 */
class XmlConfigTask extends BaseConfigTask {

    @Override
    void injectProject(Project project) {
        super.injectProject(project)
        if (!(new File(mProject.sockpuppet.xml.templatePlaceholderPath).exists())) {
            throw new GradleException("${mProject.sockpuppet.xml.templatePlaceholderPath} not exist")
        } else {
            if (TextUtils.isEmpty(mProject.sockpuppet.xml.sourcePlaceholderPath)) {
                throw new GradleException("${mProject.sockpuppet.xml.sourcePlaceholderPath} not exist")
            }
        }

        if (!(new File(mProject.sockpuppet.xml.templateBuildPath).exists())) {
            throw new GradleException("${mProject.sockpuppet.xml.templateBuildPath} not exist")
        } else {
            if (TextUtils.isEmpty(mProject.sockpuppet.xml.sourcePlaceholderPath)) {
                throw new GradleException("${mProject.sockpuppet.xml.sourceBuildPath} not exist")
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
    protected void parse(FileTree fileTree) {
        parsePlaceholder(fileTree)
        parseBuildConfig(fileTree)
    }

    /**
     * xml内容转换成BuildConfig
     * 会对boolean int string 做区分，转换成对应的类型
     * @param sourceFileTree
     */
    void parseBuildConfig(FileTree sourceFileTree) {

        def patternSet = new PatternSet();
        patternSet.include(mProject.sockpuppet.xml.sourceBuildPath)

        def resultFile = sourceFileTree.matching(patternSet)
        resultFile.eachWithIndex { File sourceFile, int sourceIndex ->
            if (isBuildConfigMatch(sourceFile)) {
                def xmlSlurper = new XmlSlurper()
                def result = xmlSlurper.parse(sourceFile)

                result.children().each {
                    def name = it.@name.toString()
                    if (it.name() == "string") {
//                    println "build string ${it.@name}-------> ${it.text()}"
                        def content = it.text()
                        if (StringUtils.isBool(content)) {//是不是bool值
                            addBuildConfigBool(name, content)
                        } else if (StringUtils.isInt(content)) {//是不是bool值
                            addBuildConfigInt(name, content)
                        } else {
                            addBuildConfigString(name, content)
                        }
                    } else if (it.name() == "string-array") {
                        addBuildConfigList(name, it.item as String[])
                    }
                }
            }
        }
    }

    /**
     * xml内容转换成placeholder
     * 只支持string转换成placeholder，不支持string-array
     * @param sourceFileTree
     */
    void parsePlaceholder(FileTree sourceFileTree) {
        def patternSet = new PatternSet();
        patternSet.include(mProject.sockpuppet.xml.sourcePlaceholderPath)

        def resultFile = sourceFileTree.matching(patternSet)

        resultFile.eachWithIndex { File picFile, int picIndex ->
            if (isPlaceholderMatch(picFile)) {
                def xmlSlurper = new XmlSlurper()
                def result = xmlSlurper.parse(picFile)

                result.children().each {
                    def name = it.@name.toString()
                    def content = it.text()
                    addPlaceholder(name, content)
                }
            }
        }
    }

    /**
     * placeholder配置和规定模版是不是一致
     * @param sourceFile
     * @return
     */
    boolean isPlaceholderMatch(File sourceFile) {
        def templateFile = new File(mProject.sockpuppet.xml.templatePlaceholderPath)
//        println("templateFile: " + templateFile.absolutePath)
        return XmlHelper.isMatchTemplate(templateFile, sourceFile)
    }

    /**
     * buildConfig配置和模版是不是一致
     * @param sourceFile
     * @return
     */
    boolean isBuildConfigMatch(File sourceFile) {
        def templateFile = new File(mProject.sockpuppet.xml.templateBuildPath)
//        println("templateFile: " + templateFile.absolutePath)
        return XmlHelper.isMatchTemplate(templateFile, sourceFile)
    }
}