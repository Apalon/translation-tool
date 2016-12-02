/*******************************************************************************
 * Copyright 2016, Apalon Apps, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.apalon.translation.tool

import org.gradle.api.Plugin
import org.gradle.api.Project

class TranslationPlugin implements Plugin<Project> {

    def translation_map = [:]

    @Override
    public void apply(Project project) {

        project.getExtensions().create("translations", TranslationPluginExtension.class);

        def android = project.getProperties().get("android")
        def source_sets = android.getProperties().get("sourceSets")
        def productFlavors = android.getProperties().get("productFlavors")
        def isFlavoredApp = !productFlavors.isEmpty()

        def projectName = project.getName()
        // processing for main resources
        def main_res_location = getResourceLocation(source_sets, "main")

        translation_map = ["${projectName}" : main_res_location]

        if (isFlavoredApp) {
            productFlavors.each { productFlavor ->
                translation_map["${projectName}${productFlavor.name}"] = getResourceLocation(source_sets, "${productFlavor.name}")
            }
        }

        createTask(project, translation_map)
    }

    def createTask(project, translation_map) {
        def task = project.getTasks().create("updateTranslations", TranslationPluginTask.class)
        task.setTranslation_map(translation_map)
    }

    def getResourceLocation(source_sets, variant) {
        return source_sets.getAt("${variant}").getProperties().get("res").getProperties().get("srcDirs").getAt(0).toString()
    }

    def getTranslation_map() {
        return translation_map
    }

    void setTranslation_map(translation_map) {
        this.translation_map = translation_map
    }
}