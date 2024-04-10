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

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;
import org.jsoup.Jsoup;

import java.util.Map;

/**
 * <p>
 * AsciidoctorJ post processor, that extracts ToC into separate file and optionally can inject content into rendered document.
 * Can be used only for HTML backend, will <b>fail</b> if used with PDF.
 * <p>
 * It is targeted to inject "front-matter" section suitable for the Hugo website generator.
 * <p>
 * Extension controlled by attributes in *.adoc file:
 * <ul>
 *     <li>hugo-header: header file name, if not set empty 'front-matter' will be used
 *     <li>multipage: split document to a separate pages
 *     <li>multipage-level: nested section level that will be used for a single page content in a multipage documents
 *     <li>multipage-header: header file name to add to a multipage documents, index page will always use cayenne-header
 *     <li>multipage-ref: reference prefix for a multipage navigation
 * </ul>
 */
public class HugoPostProcessor extends Postprocessor {

    private final InternalLogger logger;
    private DocInfo docInfo;
    private ContentWriter contentWriter;

    /**
     * Default constructor, used by Asciidoctor maven plugin
     */
    @SuppressWarnings("unused")
    public HugoPostProcessor() {
        super();
        logger = new InternalLogger(this);
    }

    /**
     * Constructor, used by Asciidoctor maven plugin
     * @param config processor configuration
     */
    @SuppressWarnings("unused")
    public HugoPostProcessor(Map<String, Object> config) {
        super(config);
        logger = new InternalLogger(this);
        logger.info(config.toString());
    }

    /**
     * Main processor method. Called by the maven plugin.
     *
     * @param document Asciidoctor document to process
     * @param output content of the document
     * @return processed content of the document
     */
    @Override
    public String process(Document document, String output) {
        docInfo = new DocInfo(document);
        contentWriter = new ContentWriter(document, logger);
        output = extractTableOfContents(output);
        output = fixupDom(output);
        output = multiPage(output);
        output = processHeader(output);
        contentWriter.flush();
        return output;
    }

    /**
     * Fix HTML elements
     *
     * @param output document content to process
     * @return processed content
     */
    protected String fixupDom(String output) {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(output);

        jsoupDoc.select(".icon-note")
                .removeClass("icon-note")
                .addClass("fa-info-circle")
                .addClass("fa-2x");

        jsoupDoc.select(".icon-tip")
                .removeClass("icon-tip")
                .addClass("fa-lightbulb-o")
                .addClass("fa-2x");

        jsoupDoc.select("code").forEach(el -> {
            String codeClass = el.attr("data-lang");
            if(!codeClass.isEmpty()) {
                el.addClass(codeClass);
            }
        });

        jsoupDoc.select("div#preamble").remove();

        return jsoupDoc.body().html();
    }

    /**
     * Extract table of contents and output it as a separate file
     *
     * @param output HTML document
     * @return HTML content without ToC
     */
    protected String extractTableOfContents(String output) {
        int start = output.indexOf("<div id=\"toc\" class=\"toc\">");
        if(start == -1) {
            // no toc found, exit
            return output;
        }

        String tocEndString = "</ul>\n</div>";
        int end = output.indexOf(tocEndString, start);
        if(end == -1) {
            // bad, no end..
            return output;
        }

        end += tocEndString.length() + 1;

        org.jsoup.nodes.Document tocDoc = Jsoup.parseBodyFragment(output.substring(start, end));
        tocDoc.select("ul").addClass("nav");
        tocDoc.select("a").addClass("nav-link");
        tocDoc.select("div#toc").addClass("toc-side");
        String toc = tocDoc.body().html();

        String docname = docInfo.documentName();
        contentWriter.addContent(docname + ".toc.html", toc);

        if(start == 0) {
            return output.substring(end);
        }
        return output.substring(0, start) + output.substring(end);
    }

    /**
     * Add `front-matter` header to the document
     *
     * @param output HTML content
     * @return HTML content with a front-matter
     */
    protected String processHeader(String output) {
        return docInfo.header() + output;
    }

    /**
     * Split document to a separate pages if required
     *
     * @param output HTML content
     * @return index page or a full content if doc is not multipage
     */
    protected String multiPage(String output) {
        MultipageProcessor processor = new MultipageProcessor(docInfo, contentWriter, new InternalLogger(this), output);
        processor.process();
        return processor.getIndexPageContent();
    }

}
