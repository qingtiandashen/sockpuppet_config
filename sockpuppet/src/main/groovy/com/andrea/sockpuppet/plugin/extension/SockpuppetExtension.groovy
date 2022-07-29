package com.andrea.sockpuppet.plugin.extension

class SockpuppetExtension {
    /**
     * 扩展名
     */
    public static final EXTENSION_NAME = "sockpuppet"

//    Iterable<String> blackList = []

    String templatePlaceholderPath = ""
    String sourcePlaceholderPath = ""
    String templateBuildPath = ""
    String sourceBuildPath = ""

    SockpuppetExtension() {
    }


    @Override
    public String toString() {
        return "SockpuppetExtension{" +
                "templatePlaceholderPath=" + templatePlaceholderPath +
                ", templateBuildPath=" + templateBuildPath +
                ", sourcePlaceholderPath=" + sourcePlaceholderPath +
                ", sourceBuildPath=" + sourceBuildPath +
                '}';
    }
}