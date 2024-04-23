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

class ProcessorHtmlContent implements ContentProcessor {
    @Override
    public String process(ProcessorContext context, String content) {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(content);
        jsoupDoc.select("code").forEach(el -> {
            String codeClass = el.attr("data-lang");
            if(!codeClass.isEmpty()) {
                el.addClass(codeClass);
            }
        });
        if(!context.docInfo().keepPreamble()) {
            jsoupDoc.select("div#preamble").remove();
        }
        return jsoupDoc.body().html();
    }
}
