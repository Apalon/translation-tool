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
package com.apalon.translation.tool.utils

import java.util.regex.Pattern

/**
 * Created by abhi on 1/11/16.
 */
class EscapingUtils {

    private static final Pattern escapingPattern = Pattern.compile("([^\\\\]?)('|\")");
    private static final String replacement = "\$1\\\\\$2";

    public static String escapeWithQuotes(String value) {
        return '"' + value + '"';
    }

    public static String escapeWithBackslash(String value) {
        return escapingPattern.matcher(value).replaceAll(replacement);
    }

    public static String unescapeQuotes(String value) {
        if (value.startsWith("\\\"") && value.endsWith("\\\"")) {
            return value.substring(2, value.length() - 2);
        }
        return value;
    }
}
