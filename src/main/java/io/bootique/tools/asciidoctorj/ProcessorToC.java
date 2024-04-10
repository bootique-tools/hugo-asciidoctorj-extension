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

/**
 * Extract table of contents and output it as a separate file
 */
class ProcessorToC implements ContentProcessor {

    @Override
    public String process(ProcessorContext context, String content) {
        int start = content.indexOf("<div id=\"toc\" class=\"toc\">");
        if(start == -1) {
            // no toc found, exit
            return content;
        }

        String tocEndString = "</ul>\n</div>";
        int end = content.indexOf(tocEndString, start);
        if(end == -1) {
            // bad, no end..
            return content;
        }

        end += tocEndString.length() + 1;

        org.jsoup.nodes.Document tocDoc = Jsoup.parseBodyFragment(content.substring(start, end));
        tocDoc.select("ul").addClass("nav");
        tocDoc.select("a").addClass("nav-link");
        tocDoc.select("div#toc").addClass("toc-side");
        String toc = tocDoc.body().html();

        String docname = context.docInfo().documentName();
        context.writer().addContent(docname + ".toc.html", toc);

        if(start == 0) {
            return content.substring(end);
        }
        return content.substring(0, start) + content.substring(end);
    }
}
