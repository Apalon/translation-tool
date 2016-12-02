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
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class TranslationPluginTask extends DefaultTask {

    def translation_map;

    @TaskAction
    public void translate() {

        TranslationPluginExtension ext = getProject().getExtensions().findByType(TranslationPluginExtension.class)

        if(ext == null || ext.getSpreadsheetId() == null || ext.getSpreadsheetId() == '') {
            throw new Exception("Error: SpreadsheetId not defined")
        }

        try {
            def translationMap = getTranslation_map()
            translationMap.keySet().each { key ->
               TranslateTool.run(key, translationMap.get(key), ext.getSpreadsheetId())
            }
        } catch (Exception e) {
            throw new Exception("Error in translations "+ e.printStackTrace());
        }
    }

    def getTranslation_map() {
        return translation_map
    }

    void setTranslation_map(translation_map) {
        this.translation_map = translation_map
    }
}
