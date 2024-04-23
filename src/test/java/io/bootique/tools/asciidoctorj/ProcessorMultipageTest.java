package io.bootique.tools.asciidoctorj;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProcessorMultipageTest {

    ProcessorMultipage multipage = new ProcessorMultipage();

    @Test
    void noProcessing() throws IOException {
        String content = getTestContent();
        ProcessorContext context = getContext(false);

        String processed = multipage.process(context, content);
        assertEquals(content, processed);
    }

    @Test
    void defaultMultipageConfig() throws IOException {
        String content = getTestContent();
        ProcessorContext context = getContext(true);

        String processed = multipage.process(context, content);
        assertEquals("<div id=\"preamble\">\n" +
                " <div class=\"sectionbody\">\n" +
                "  <div class=\"paragraph\">\n" +
                "   <p>\"DFLib\" (short for \"DataFrame Library\") is a lightweight, pure Java implementation of <code>DataFrame</code>. <code>DataFrame</code> is a very common structure in data science and Big Data worlds. It provides operations like search, filtering, joins, aggregations, statistical functions, etc., that are reminiscent of SQL (also of Excel), except you run them in your app over dynamic in-memory data sets.</p>\n" +
                "  </div>\n" +
                " </div>\n" +
                "</div>\n" +
                "<ul>\n" +
                "<li><a href=\"/getting_started_with_dflib\">Getting Started with DFLib</a>\n" +
                "<li><a href=\"/main_data_structures\">Main Data Structures</a>\n" +
                "</ul>", processed);
    }

    private static ProcessorContext getContext(boolean multipage) {
        DocInfo docInfo = mock(DocInfo.class);

        when(docInfo.isMultipage()).thenReturn(multipage);
        when(docInfo.multipageHeader()).thenReturn("---\n---\n");
        when(docInfo.multipageRef()).thenReturn("");
        when(docInfo.documentName()).thenReturn("");

        ContentWriter writer = mock(ContentWriter.class);
        InternalLogger logger = mock(InternalLogger.class);
        return new ProcessorContext(docInfo, writer, logger);
    }

    private String getTestContent() throws IOException {
        try(InputStream is = getClass().getClassLoader().getResourceAsStream("docs.html")) {
            StringBuilder expected = new StringBuilder();
            try(BufferedReader resource = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = resource.readLine()) != null) {
                    expected.append(line);
                }
            }
            return expected.toString();
        }
    }

}