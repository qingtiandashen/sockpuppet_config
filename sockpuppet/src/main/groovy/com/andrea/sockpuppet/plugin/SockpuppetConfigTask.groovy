package com.andrea.sockpuppet.plugin

import com.andrea.sockpuppet.plugin.extension.SockpuppetExtension
import com.andrea.sockpuppet.plugin.utils.BiLog
import com.andrea.sockpuppet.plugin.utils.StringUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternSet


class SockpuppetConfigTask implements Plugin<Project> {

    static Project mProject;

    @Override
    void apply(Project project) {

        BiLog.init(project)
        mProject = project;

        injectSockExtensions()

        if (!mProject.plugins.hasPlugin('com.android.application')) {
            throw new GradleException('com.android.application required')
        }

        mProject.gradle.afterProject {
            System.out.println("------------------auto config setting start----------------------");
            System.out.println("2---templatePlaceholderPath--${mProject.sockpuppet.templatePlaceholderPath}");
            System.out.println("2---templateBuildPath--${mProject.sockpuppet.templateBuildPath}");
            System.out.println("2---sourcePlaceholderPath--${mProject.sockpuppet.sourcePlaceholderPath}");
            System.out.println("2---sourceBuildPath--${mProject.sockpuppet.sourceBuildPath}");

            if (!(new File(mProject.sockpuppet.templatePlaceholderPath).exists())) {
                throw new GradleException("${mProject.sockpuppet.templatePlaceholderPath} not exist")
            }

            if (!(new File(mProject.sockpuppet.templateBuildPath).exists())) {
                throw new GradleException("${mProject.sockpuppet.templateBuildPath} not exist")
            }

            //获取各module
            mProject.getRootProject().getSubprojects().eachWithIndex { Project entry, int i ->
                def fileTree = entry.layout.projectDirectory.asFileTree
                parsePlaceholder(fileTree)
                parseBuildConfig(fileTree)
            }
            System.out.println("------------------auto config setting end----------------------");
        }
    }

    private static void parseBuildConfig(FileTree sourceFileTree) {
//        def file = entry.layout.projectDirectory.asFileTree


        def patternSet = new PatternSet();
        patternSet.include(mProject.sockpuppet.sourceBuildPath)

        def resultFile = sourceFileTree.matching(patternSet)
        resultFile.eachWithIndex { File picFile, int picIndex ->
            if (isBuildConfigMatch(picFile)) {
                def xmlSlurper = new XmlSlurper()
                def result = xmlSlurper.parse(picFile)

                result.children().each {
                    if (it.name() == "string") {
//                    it.text()
//                    println "build string ${it.@name}-------> ${it.text()}"
                        def content = it.text()
                        if (StringUtils.isBool(content)) {//是不是bool值
                            mProject.android.defaultConfig.buildConfigField("boolean", it.@name.toString(), content)
                        } else if (StringUtils.isInt(content)) {//是不是bool值
                            mProject.android.defaultConfig.buildConfigField("int", it.@name.toString(), content)
                        } else {
                            mProject.android.defaultConfig.buildConfigField("String", it.@name.toString(), "\"${content}\"")
                        }
                    } else if (it.name() == "string-array") {
//                        println "string-array:    ${it.name()} --> ${it.@name}-->${it.item.size()}"

                        def sb = new StringBuilder()
                        sb.append("new java.util.ArrayList<String>()")
                        sb.append("{")
                        sb.append("{")

                        for (index in 0..<it.item.size()) {
                            sb.append("add(\"${it.item[index].text()}\");")
                        }

                        sb.append("}")
                        sb.append("}")
                        mProject.android.defaultConfig.buildConfigField("java.util.List<String>", it.@name.toString(), sb.toString())
                    }
                }
            }
        }
    }

    private static void parsePlaceholder(FileTree sourceFileTree) {
        def patternSet = new PatternSet();
        patternSet.include(mProject.sockpuppet.sourcePlaceholderPath)

        def resultFile = sourceFileTree.matching(patternSet)

        resultFile.eachWithIndex { File picFile, int picIndex ->
            if (isPlaceholderMatch(picFile)) {
                def xmlSlurper = new XmlSlurper()
                def result = xmlSlurper.parse(picFile)

                result.children().each {
                    if (it.name() == "string") {
                        mProject.android.defaultConfig.manifestPlaceholders.put(it.@name.toString(), it.text())
                    } else if (it.name() == "string-array") {
//                        println "string-array:    ${it.name()} --> ${it.@name}-->${it.item.size()}"
//                        for (index in 0..<it.item.size()) {
////                            println("it.item[${index}].text(): " + it.item[index].text())
//                        }
                    }
                }
            }
        }
    }

    private static boolean isPlaceholderMatch(File sourceFile) {
        def templateFile = new File(mProject.sockpuppet.templatePlaceholderPath)
//        println("templateFile: " + templateFile.absolutePath)
        return isMatchTemplate(templateFile, sourceFile)
    }

    private static boolean isBuildConfigMatch(File sourceFile) {
        def templateFile = new File(mProject.sockpuppet.templateBuildPath)
//        println("templateFile: " + templateFile.absolutePath)
        return isMatchTemplate(templateFile, sourceFile)
    }

    /**
     * 比较两个文件的目录是否匹配
     * @param templateFile
     * @param sourceFile
     * @return
     */
    private static boolean isMatchTemplate(File templateFile, File sourceFile) {
        //有就要和模版对比，不一致就报错
        def xmlSlurperTemplate = new XmlSlurper()
        def templateResult = xmlSlurperTemplate.parse(templateFile)

        def templateMap = new HashMap<String, String>()

        templateResult.children().each {
            templateMap.put(it.@name.toString(), it.name())//存入name和类型
        }

        def xmlSlurper = new XmlSlurper()
        def result = xmlSlurper.parse(sourceFile)

        result.children().each {
            def key = it.@name.toString()//<string name="abc"/>
            if (templateMap.containsKey(key)) {
                if (templateMap.get(key) == it.name()) {
                    templateMap.remove(key)
                } else {
                    throw new GradleException("${sourceFile.absolutePath} ${key} attribute not match")
                }
            } else {
                throw new GradleException("${sourceFile.absolutePath} source xml contains key more than template xml")
            }
        }

        if (templateMap.size() != 0) {
            throw new GradleException("${sourceFile.absolutePath} source xml not match template xml")
        }

        return true
    }

    /**
     * 扩展extension
     * @param project
     */
    private static void injectSockExtensions() {
        //添加lfPlugin的配置支持
        mProject.extensions.create("sockpuppet", SockpuppetExtension)
    }
}