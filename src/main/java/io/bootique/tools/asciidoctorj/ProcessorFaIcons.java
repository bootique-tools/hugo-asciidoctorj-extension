/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.tools.asciidoctorj;

import org.jsoup.Jsoup;

import java.util.HashMap;
import java.util.Map;

/**
 * Convert icons to a font-awesome compatible variant
 */
class ProcessorFaIcons implements ContentProcessor {

    private static final Map<String, String> ICONS_REPLACEMENT = new HashMap<>();

    static {
        ICONS_REPLACEMENT.put("icon-tip", "fa-lightbulb-o");
        ICONS_REPLACEMENT.put("icon-note", "fa-info-circle");
        ICONS_REPLACEMENT.put("icon-important", "fa-exclamation-circle");
        ICONS_REPLACEMENT.put("icon-warning", "fa-exclamation-triangle");
        ICONS_REPLACEMENT.put("icon-caution", "fa-exclamation-triangle");
    }

    @Override
    public String process(ProcessorContext context, String content) {
        if (!context.docInfo().convertToFa()) {
            return content;
        }
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(content);
        ICONS_REPLACEMENT.forEach((key, value) ->
                jsoupDoc.select("." + key)
                        .removeClass(key)
                        .addClass(value)
                        .addClass("fa-2x"));
        return jsoupDoc.body().html();
    }
}
