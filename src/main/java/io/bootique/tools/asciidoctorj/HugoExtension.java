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

import java.util.List;
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
@SuppressWarnings("unused")
public class HugoExtension extends Postprocessor {

    private final InternalLogger logger;

    private final static List<ContentProcessor> CONTENT_PROCESSORS = List.of(
            new ProcessorToC(),
            new ProcessorFaIcons(),
            new ProcessorHtmlContent(),
            new ProcessorMultipage(),
            new ProcessorFrontMatter()
    );

    /**
     * Default constructor, used by Asciidoctor maven plugin
     */
    @SuppressWarnings("unused")
    public HugoExtension() {
        super();
        logger = new InternalLogger(this);
    }

    /**
     * Constructor, used by Asciidoctor maven plugin
     * @param config processor configuration
     */
    @SuppressWarnings("unused")
    public HugoExtension(Map<String, Object> config) {
        super(config);
        logger = new InternalLogger(this);
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
        ContentWriter contentWriter = new ContentWriter(document, logger);
        ProcessorContext context = new ProcessorContext(new DocInfo(document), contentWriter, logger);
        for(ContentProcessor processor : CONTENT_PROCESSORS) {
            output = processor.process(context, output);
        }
        contentWriter.flush();
        return output;
    }
}
