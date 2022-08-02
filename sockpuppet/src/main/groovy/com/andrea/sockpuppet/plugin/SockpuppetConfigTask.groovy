package com.andrea.sockpuppet.plugin

import com.andrea.sockpuppet.plugin.extension.JsonExtension
import com.andrea.sockpuppet.plugin.extension.SockpuppetExtension
import com.andrea.sockpuppet.plugin.extension.XmlExtension
import com.andrea.sockpuppet.plugin.task.JsonConfigTask
import com.andrea.sockpuppet.plugin.task.XmlConfigTask
import com.andrea.sockpuppet.plugin.utils.BiLog
import com.andrea.sockpuppet.plugin.utils.StringUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project


class SockpuppetConfigTask implements Plugin<Project> {

    static Project mProject;

    @Override
    void apply(Project project) {

        BiLog.init(project)
        mProject = project;

        injectSockExtensions()

        //只支持主module
        if (!mProject.plugins.hasPlugin('com.android.application')) {
            throw new GradleException('com.android.application required')
        }

        //需要在afterProject生命周期里添加placeholder和buildConfig才生效
        mProject.gradle.afterProject {
            System.out.println("------------------auto config setting start----------------------");

            if (!StringUtils.isEmpty(mProject.sockpuppet.xml.templatePlaceholderPath) ||
                    !StringUtils.isEmpty(mProject.sockpuppet.xml.templateBuildPath)) {
                def task = new XmlConfigTask()
                task.injectProject(mProject)
                task.apply()
            }

            if (!StringUtils.isEmpty(mProject.sockpuppet.json.templatePath)) {
                def task = new JsonConfigTask()
                task.injectProject(mProject)
                task.apply()
            }
            System.out.println("------------------auto config setting end----------------------");
        }
    }

    /**
     * 扩展extension
     * @param project
     */
    private static void injectSockExtensions() {
        //添加lfPlugin的配置支持
        mProject.extensions.create("sockpuppet", SockpuppetExtension)

//        //添加compress配置
        mProject.sockpuppet.extensions.create("xml", XmlExtension)
//        //添加tiny的配置
        mProject.sockpuppet.extensions.create("json", JsonExtension)
    }
}