##Translation Tool Plugin

With the translation tool, translating strings has become an intern's job (yes, as easy as making coffee!). 

The plugin can be applied to any project by adding the following lines to the build.gradle
`apply 'translationtool'`
and using the library
`com.apalon.translationtool:translation-tool:1.4.0`


However, the plugin assumes the following:

- The master translation file is present as a [Google Spreadsheet](https://docs.google.com/spreadsheets/d/1Rt7aOgI4DN8il8Vt4k2_Q3cerMXgutVyngp-8GvQusA)
- This file is publicly readable but can be written only when given permission.
- The Google Spreadsheet has separate worksheet for each project/flavor. 
- If there is a translatable string to be added to the project, it should be added to the corresponding worksheet.
- Since these strings.xml files are always generated at build time, they should never be manually changed and they should not be checked in to the repo with the code. 
- The older strings which are not translated/translatable are placed in a file called `strings-untranslated.xml`. Developers should use this file to add additional strings which cannot be translated.