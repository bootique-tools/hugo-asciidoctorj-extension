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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class MultipageProcessor {
    private final DocInfo docInfo;
    private final InternalLogger logger;
    private final ContentWriter contentWriter;
    private String indexPageContent;

    MultipageProcessor(DocInfo docInfo, ContentWriter contentWriter, InternalLogger logger, String indexPageContent) {
        this.docInfo = docInfo;
        this.logger = logger;
        this.contentWriter = contentWriter;
        this.indexPageContent = indexPageContent;
    }

    void process() {
        if (!docInfo.isMultipage()) {
            return;
        }

        Document jsoupDoc = Jsoup.parseBodyFragment(indexPageContent);
        List<Section> rootSections = sectionsOnLevel(jsoupDoc, jsoupDoc, 1);

        buildIndexPage(rootSections);
        buildContentPages(rootSections);
        buildTocPage(rootSections);
    }

    private void buildIndexPage(List<Section> sections) {
        StringBuilder sb = new StringBuilder();
        sectionListHtml(sections, sb);
        this.indexPageContent = sb.toString();
    }

    private void buildTocPage(List<Section> indexSections) {
        StringBuilder sb = new StringBuilder("<div id=\"toc\" class=\"toc toc-side\">");
        buildTocLevel(sb, indexSections, 1);
        sb.append("</div>");
        contentWriter.addContent(docInfo.documentName() + ".toc.html", sb.toString());
    }

    private void buildContentPages(List<Section> sections) {
        sections.forEach(s -> {
            contentWriter.addContent(s.documentName(), docInfo.documentName(), s.content());
            buildContentPages(s.subsections());
        });
    }

    private void buildTocLevel(StringBuilder sb, List<Section> sections, int level) {
        sb.append("\n<ul class=\"sectlevel").append(level).append(" nav\">\n");
        sections.forEach(s -> {
            sb.append("  <li>")
                    .append("<a href=\"").append(ref(s.id())).append("\"")
                    .append(" id=\"").append(s.id()).append("\"")
                    .append(" class=\"nav-link\">").append(s.title()).append("</a>");
            if(!s.subsections().isEmpty()) {
                buildTocLevel(sb, s.subsections(), level + 1);
            }
            sb.append("</li>\n");
        });
        sb.append("</ul>\n");
    }

    private String fixAnchors(Element root, Element element) {
        element.select("a:not(.anchor)").forEach(el -> {
            String ref = el.attr("href");
            if (!ref.startsWith("#")) { // we are interested only in cross-docs references
                return;
            }

            String id = ref.substring(1);
            Element sectionRoot = findSectionRoot(root, ref, docInfo.multipageLevel());
            if(sectionRoot != null) {
                if(!id.equals(sectionRoot.id())) {
                    id = sectionRoot.id() + "#" + id;
                }
            }
            el.attr("href", ref(id));
        });
        return element.outerHtml();
    }

    private List<Section> sectionsOnLevel(Element root, Element element, int currentLevel) {
        return element.select(".sect" + currentLevel).stream()
                .map(el -> {
                    Element sectionHeader = getSectionHeader(el, currentLevel);
                    if (sectionHeader == null) {
                        logger.warn("No header for a section " + el.children().iterator().next().outerHtml());
                        return null;
                    }
                    String sectionId = sectionHeader.id();
                    String title = el.child(0).text();
                    if (currentLevel < docInfo.multipageLevel()) {
                        List<Section> subsections = sectionsOnLevel(root, el, currentLevel + 1);
                        String content1 = subsections.isEmpty()
                                ? header(title) + fixAnchors(root, el)
                                : buildIndexSection(title, subsections);
                        return new Section(sectionId, title, content1, subsections);
                    } else {
                        return new Section(sectionId, title, header(title) + fixAnchors(root, el));
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String header(String title) {
        int chapterNumber = title.indexOf(". ");
        if(chapterNumber != -1) {
            title = title.substring(chapterNumber + 2);
        }
        return docInfo.multipageHeader()
                .replaceAll("\\{title}", title);
    }

    private String buildIndexSection(String title, List<Section> subsections) {
        StringBuilder sb = new StringBuilder(header(title));
        sb.append("<div><h2>").append(title).append("</h2>\n");
        sectionListHtml(subsections, sb);
        return sb.append("</div>\n").toString();
    }

    private void sectionListHtml(List<Section> subsections, StringBuilder sb) {
        sb.append("<ul>\n");
        subsections.forEach(s -> {
            String innerSectionId = s.id();
            sb.append("<li><a href=\"").append(ref(innerSectionId)).append("\">")
                    .append(s.title())
                    .append("</a>\n");
        });
        sb.append("</ul>");
    }

    private String ref(String sectionId) {
        return docInfo.multipageRef() + docInfo.documentName() + "/" + sectionId;
    }

    String getIndexPageContent() {
        return indexPageContent;
    }

    private Element findSectionRoot(Element element, String selector, int level) {
        for (int l = level; l > 0; l--) {
            Element target = element.select(selector).first();
            if (target != null) {
                while (target.parent() != null) {
                    target = target.parent();
                    if (target.hasClass("sect" + l)) {
                        Element inner = getSectionHeader(target, l);
                        if (inner != null) {
                            return inner;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Element getSectionHeader(Element target, int l) {
        String targetTag = "h" + (l + 1);
        for (Element inner : target.children()) {
            if (targetTag.equals(inner.tag().getName())) {
                return inner;
            }
        }
        logger.warn("Asciidoc multipage processor: No header for section " + target.id());
        return null;
    }

}
