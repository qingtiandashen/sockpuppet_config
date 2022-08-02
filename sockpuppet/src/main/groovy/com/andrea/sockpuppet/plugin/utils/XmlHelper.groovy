package com.andrea.sockpuppet.plugin.utils;

import org.gradle.api.GradleException;

public class XmlHelper {

    /**
     * 比较两个文件的目录是否匹配
     *
     * @param templateFile
     * @param sourceFile
     * @return
     */
    public static boolean isMatchTemplate(File templateFile, File sourceFile) {
        //有就要和模版对比，不一致就报错
        def xmlSlurperTemplate = new XmlSlurper()
        def templateResult = xmlSlurperTemplate.parse(templateFile)

        def templateMap = new HashMap<String, String>()

        templateResult.children().each {
            templateMap.put(it. @name.toString(),it.name())//存入name和类型
        }

        def xmlSlurper = new XmlSlurper()
        def result = xmlSlurper.parse(sourceFile)

        result.children().each {
            def key = it. @name.toString()//<string name="abc"/>
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
}
