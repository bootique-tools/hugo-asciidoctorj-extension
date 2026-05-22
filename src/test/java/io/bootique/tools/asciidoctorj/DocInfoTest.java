package io.bootique.tools.asciidoctorj;

import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocInfoTest {

    private static final String EMPTY_FRONT_MATTER = "---\n---\n\n";

    @Test
    void readsHeaderDirectlyBeforeResolving(@TempDir Path tempDir) {
        Path docdir = tempDir.resolve("docdir");
        Path baseDir = tempDir.resolve("baseDir");
        Document document = document(baseDir);
        InternalLogger logger = mock(InternalLogger.class);

        String header = "---\ntitle: Test\n---\n";
        String headerFile = "header.html";
        String docdirHeaderFile = docdir.resolve(headerFile).toString();
        String baseDirHeaderFile = baseDir.resolve(headerFile).toString();

        when(document.getAttribute("hugo-header", "")).thenReturn(headerFile);
        when(document.getAttribute("docdir", "")).thenReturn(docdir.toString());
        when(document.readAsset(eq(headerFile), anyMap())).thenReturn(header);

        DocInfo docInfo = new DocInfo(document, logger);

        assertEquals(header, docInfo.header());
        verify(document, never()).readAsset(eq(docdirHeaderFile), anyMap());
        verify(document, never()).readAsset(eq(baseDirHeaderFile), anyMap());
    }

    @Test
    void readsRelativeHeaderFromDocdir(@TempDir Path tempDir) {
        Path docdir = tempDir.resolve("docdir");
        Path baseDir = tempDir.resolve("baseDir");
        Document document = document(baseDir);
        InternalLogger logger = mock(InternalLogger.class);

        String header = "---\ntitle: Test\n---\n";
        String headerFile = "header.html";
        String docdirHeaderFile = docdir.resolve(headerFile).toString();
        String baseDirHeaderFile = baseDir.resolve(headerFile).toString();

        when(document.getAttribute("hugo-header", "")).thenReturn(headerFile);
        when(document.getAttribute("docdir", "")).thenReturn(docdir.toString());
        when(document.readAsset(eq(docdirHeaderFile), anyMap())).thenReturn(header);

        DocInfo docInfo = new DocInfo(document, logger);

        assertEquals(header, docInfo.header());
        verify(document, never()).readAsset(eq(baseDirHeaderFile), anyMap());
    }

    @Test
    void readsRelativeMultipageHeaderFromDocdir(@TempDir Path tempDir) {
        Path docdir = tempDir.resolve("docdir");
        Document document = document(tempDir.resolve("baseDir"));
        InternalLogger logger = mock(InternalLogger.class);

        String header = "---\ntitle: Test Page\n---\n";
        String headerFile = "multipage-header.html";
        String resolvedHeaderFile = docdir.resolve(headerFile).toString();

        when(document.getAttribute("hugo-multipage-header", "")).thenReturn(headerFile);
        when(document.getAttribute("docdir", "")).thenReturn(docdir.toString());
        when(document.readAsset(eq(resolvedHeaderFile), anyMap())).thenReturn(header);

        DocInfo docInfo = new DocInfo(document, logger);

        assertEquals(header, docInfo.multipageHeader());
    }

    @Test
    void keepsEmptyFrontMatterWhenHeaderMissing(@TempDir Path tempDir) {
        Document document = document(tempDir.resolve("baseDir"));
        InternalLogger logger = mock(InternalLogger.class);

        when(document.getAttribute("hugo-header", "")).thenReturn("missing.html");
        when(document.getAttribute("docdir", "")).thenReturn(tempDir.resolve("docdir").toString());
        when(document.readAsset(anyString(), anyMap())).thenReturn(null);

        DocInfo docInfo = new DocInfo(document, logger);

        assertEquals(EMPTY_FRONT_MATTER, docInfo.header());
        verify(logger).warn("Header file 'missing.html' not found. Using a default header.");
    }

    @Test
    void keepsEmptyFrontMatterWhenHeaderAttributeAbsent(@TempDir Path tempDir) {
        Document document = document(tempDir);
        InternalLogger logger = mock(InternalLogger.class);

        DocInfo docInfo = new DocInfo(document, logger);

        assertEquals(EMPTY_FRONT_MATTER, docInfo.header());
        verify(document, never()).readAsset(anyString(), anyMap());
        verify(logger, never()).warn(anyString());
    }

    @Test
    void readsAbsoluteHeaderPath(@TempDir Path tempDir) {
        Document document = document(tempDir);
        InternalLogger logger = mock(InternalLogger.class);

        String header = "---\ntitle: Test\n---\n";
        String headerFile = tempDir.resolve("header.html").toString();

        when(document.getAttribute("hugo-header", "")).thenReturn(headerFile);
        when(document.readAsset(eq(headerFile), anyMap())).thenReturn(header);

        DocInfo docInfo = new DocInfo(document, logger);

        assertEquals(header, docInfo.header());
    }

    @Test
    void fallsBackToBaseDirWhenDocdirMissing(@TempDir Path tempDir) {
        Document document = document(tempDir);
        InternalLogger logger = mock(InternalLogger.class);

        String header = "---\ntitle: Test\n---\n";
        String headerFile = "header.html";
        String resolvedHeaderFile = tempDir.resolve(headerFile).toString();

        when(document.getAttribute("hugo-header", "")).thenReturn(headerFile);
        when(document.readAsset(eq(resolvedHeaderFile), anyMap())).thenReturn(header);

        DocInfo docInfo = new DocInfo(document, logger);

        assertEquals(header, docInfo.header());
    }

    @Test
    void fallsBackToBaseDirWhenDocdirHeaderMissing(@TempDir Path tempDir) {
        Path docdir = tempDir.resolve("docdir");
        Path baseDir = tempDir.resolve("baseDir");
        Document document = document(baseDir);
        InternalLogger logger = mock(InternalLogger.class);

        String header = "---\ntitle: Test\n---\n";
        String headerFile = "header.html";
        String baseDirHeaderFile = baseDir.resolve(headerFile).toString();

        when(document.getAttribute("hugo-header", "")).thenReturn(headerFile);
        when(document.getAttribute("docdir", "")).thenReturn(docdir.toString());
        when(document.readAsset(eq(baseDirHeaderFile), anyMap())).thenReturn(header);

        DocInfo docInfo = new DocInfo(document, logger);

        assertEquals(header, docInfo.header());
    }

    private Document document(Path baseDir) {
        Document document = mock(Document.class);
        when(document.getOptions()).thenReturn(options(baseDir));
        when(document.getAttribute("hugo-header", "")).thenReturn("");
        when(document.getAttribute("hugo-multipage", "false")).thenReturn("false");
        when(document.getAttribute("hugo-multipage-level", 1)).thenReturn("1");
        when(document.getAttribute("hugo-multipage-header", "")).thenReturn("");
        when(document.getAttribute("hugo-multipage-ref", "")).thenReturn("");
        when(document.getAttribute("hugo-font-awesome-icons", "true")).thenReturn("true");
        when(document.getAttribute("hugo-keep-preamble", "false")).thenReturn("false");
        when(document.getAttribute("docdir", "")).thenReturn("");
        return document;
    }

    private Map<Object, Object> options(Path baseDir) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("docname", "test-doc");

        Map<Object, Object> options = new HashMap<>();
        options.put(Options.ATTRIBUTES, attributes);
        options.put(Options.BASEDIR, baseDir.toString());
        return options;
    }
}
