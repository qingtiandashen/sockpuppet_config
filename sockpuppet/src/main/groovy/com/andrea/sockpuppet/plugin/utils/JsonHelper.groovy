package com.andrea.sockpuppet.plugin.utils

import com.andrea.sockpuppet.plugin.data.Config
import groovy.json.JsonSlurper;
import org.gradle.api.GradleException;

public class JsonHelper {

    private static void removeMap(File sourceFile, Map<String, String> map, List list) {
//        def list = it[Config.JSON_LIST]
        list.each { item ->
            def val = item[Config.JSON_VALUE]
            if (val instanceof ArrayList) {
                if (map.containsKey(item[Config.JSON_NAME])) {
                    if (map.get(item[Config.JSON_NAME]) == Config.VALUE_LIST) {
                        map.remove(item[Config.JSON_NAME])
                    }
                } else {
                    throw new GradleException("${sourceFile.absolutePath} source json contains key more than template xml")
//                    map.put(item[Config.JSON_NAME], VALUE_LIST)
                }
            } else if (val instanceof Map) {
                if (map.containsKey(item[Config.JSON_NAME])) {
                    if (map.get(item[Config.JSON_NAME]) == Config.VALUE_MAP) {
                        map.remove(item[Config.JSON_NAME])
                    }
                } else {
                    throw new GradleException("${sourceFile.absolutePath} source json contains key more than template xml")
//                    map.put(item[Config.JSON_NAME], VALUE_MAP)
                }
            } else {
                if (map.containsKey(item[Config.JSON_NAME])) {
                    if (map.get(item[Config.JSON_NAME]) == Config.VALUE_STRING) {
                        map.remove(item[Config.JSON_NAME])
                    }
                } else {
                    throw new GradleException("${sourceFile.absolutePath} source json contains key more than template xml")
//                    map.put(item[Config.JSON_NAME], VALUE_STRING)
                }
            }
        }
    }

    private static void addMap(Map<String, String> map, List list) {
        list.each { item ->
            def val = item[Config.JSON_VALUE]
            if (val instanceof ArrayList) {
                map.put(item[Config.JSON_NAME], Config.VALUE_LIST)
            } else if (val instanceof Map) {
                map.put(item[Config.JSON_NAME], Config.VALUE_MAP)
            } else {
                map.put(item[Config.JSON_NAME], Config.VALUE_STRING)
            }
        }
    }

    /**
     * 比较两个文件的目录是否匹配
     *
     * @param templateFile
     * @param sourceFile
     * @return
     */
    public static boolean isMatchTemplate(File templateFile, File sourceFile) {
        def placeholderMap = new HashMap<String, String>()
        def configMap = new HashMap<String, String>()

        def templateJsonSlurper = new JsonSlurper()
        def templateResult = templateJsonSlurper.parse(templateFile)
        if (templateResult instanceof ArrayList) {
            templateResult.each { it ->
                println "build string ${it}------->"
                if (it[Config.JSON_TYPE] == Config.KEY_PLACEHOLDER) {
                    addMap(placeholderMap, it[Config.JSON_LIST])
                } else if (it[Config.JSON_TYPE] == Config.KEY_BUILD_CONFIG) {
                    addMap(configMap, it[Config.JSON_LIST])
                }
            }
        }

        def sourceJsonSlurper = new JsonSlurper()
        def sourceResult = sourceJsonSlurper.parse(templateFile)
        if (sourceResult instanceof ArrayList) {
            sourceResult.each { it ->
                println "build string ${it}------->"
                if (it[Config.JSON_TYPE] == Config.KEY_PLACEHOLDER) {
                    removeMap(sourceFile, placeholderMap, it[Config.JSON_LIST])
                } else if (it[Config.JSON_TYPE] == Config.KEY_BUILD_CONFIG) {
                    removeMap(sourceFile, configMap, it[Config.JSON_LIST])
                }
            }
        }

        if (placeholderMap.size() != 0) {
            throw new GradleException("${sourceFile.absolutePath} source xml not match template xml")
        }

        if (configMap.size() != 0) {
            throw new GradleException("${sourceFile.absolutePath} source xml not match template xml")
        }

        return true
    }


}
