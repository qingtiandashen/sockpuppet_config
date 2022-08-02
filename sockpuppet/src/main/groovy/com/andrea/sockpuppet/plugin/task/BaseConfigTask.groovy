package com.andrea.sockpuppet.plugin.task

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileTree

abstract class BaseConfigTask {

    Project mProject

    void injectProject(Project project) {
        this.mProject = project
    }

    abstract void apply();

    protected abstract void parse(FileTree sourceFileTree)

    /**
     * 获取调用方法的返回值
     * @param key
     * @param value
     */
    protected String getFuncResult(String funcName, List<String> args) {
        return getFuncResult(funcName, args as String[])
    }

    /**
     * 获取调用方法的返回值
     * @param key
     * @param value
     */
    protected String getFuncResult(String funcName, Object... args) {
        def result = ""
        try {
            result = mProject.invokeMethod(funcName, args)
        } catch (Exception ignore) {
            throw new GradleException("${funcName} method not exist")
        }
        return result
    }

    /**
     * 添加placeholder暂时只支持String
     * @param key
     * @param value
     */
    protected void addPlaceholder(String key, String value) {
        mProject.android.defaultConfig.manifestPlaceholders.put(key, value)
    }

    /**
     * 添加bool类型的BuildConfig
     * @param key
     * @param value
     */
    protected void addBuildConfigBool(String key, String value) {
        mProject.android.defaultConfig.buildConfigField("boolean", key, value)
    }

    /**
     * 添加int类型的BuildConfig
     * @param key
     * @param value
     */
    protected void addBuildConfigInt(String key, String value) {
        mProject.android.defaultConfig.buildConfigField("int", key, value)
    }

    /**
     * 添加String类型的BuildConfig
     * @param key
     * @param value
     */
    protected void addBuildConfigString(String key, String value) {
        mProject.android.defaultConfig.buildConfigField("String", key, "\"${value}\"")
    }

    /**
     * 添加List<String>类型的BuildConfig
     * @param key
     * @param value
     */
    protected void addBuildConfigList(String key, String[] values) {
        def sb = new StringBuilder()
        sb.append("new java.util.ArrayList<String>()")
        sb.append("{")
        sb.append("{")

        for (index in 0..<values.size()) {
            sb.append("add(\"${values[index]}\");")
        }

        sb.append("}")
        sb.append("}")
        mProject.android.defaultConfig.buildConfigField("java.util.List<String>", key, sb.toString())
    }
}