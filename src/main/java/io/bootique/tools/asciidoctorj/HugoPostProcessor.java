

package io.bootique.tools;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;
import org.jsoup.Jsoup;

import java.util.Map;

/**
 * <p>
 * AsciidoctorJ post processor, that extracts ToC into separate file and optionally can inject content into rendered document.
 * Can be used only for HTML backend, will <b>fail</b> if used with PDF.
 * <p>
 * It is targeted to inject "front-matter" section suitable for cayenne website tools.
 * <p>
 * Extension controlled by attributes in *.adoc file:
 * <ul>
 *     <li>cayenne-header: header file name, if not set empty 'front-matter' will be used
 *     <li>multipage: split document to a separate pages
 *     <li>multipage-level: nested section level that will be used for a single page content in a multipage documents
 *     <li>multipage-header: header file name to add to a multipage documents, index page will always use cayenne-header
 *     <li>multipage-ref: reference prefix for a multipage navigation
 * </ul>
 *
 * @since 4.1
 */
public class HugoPostProcessor extends Postprocessor {
    private final InternalLogger logger;
    private DocInfo docInfo;
    private ContentWriter contentWriter;

    @SuppressWarnings("unused")
    public HugoPostProcessor() {
        super();
        logger = new InternalLogger(this);
    }

    @SuppressWarnings("unused")
    public HugoPostProcessor(Map<String, Object> config) {
        super(config);
        logger = new InternalLogger(this);
    }

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

    private String fixupDom(String output) {
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

    protected String processHeader(String output) {
        return docInfo.header() + output;
    }

    private String multiPage(String output) {
        MultipageProcessor processor = new MultipageProcessor(docInfo, contentWriter, new InternalLogger(this), output);
        processor.process();
        return processor.getIndexPageContent();
    }

}
