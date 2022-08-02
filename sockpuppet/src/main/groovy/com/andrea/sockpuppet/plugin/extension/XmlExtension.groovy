package com.andrea.sockpuppet.plugin.extension

class XmlExtension {

    String templatePlaceholderPath = ""
    String sourcePlaceholderPath = ""
    String templateBuildPath = ""
    String sourceBuildPath = ""

    XmlExtension() {
    }


    @Override
    public String toString() {
        return "XmlExtension{" +
                "templatePlaceholderPath=" + templatePlaceholderPath +
                ", templateBuildPath=" + templateBuildPath +
                ", sourcePlaceholderPath=" + sourcePlaceholderPath +
                ", sourceBuildPath=" + sourceBuildPath +
                '}';
    }
}