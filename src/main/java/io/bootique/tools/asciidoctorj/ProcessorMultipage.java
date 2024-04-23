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
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Split document to a separate pages if required
 */
class ProcessorMultipage implements ContentProcessor {

    private int weightCounter = 1;

    ProcessorContext context;

    public String process(ProcessorContext context, String indexPageContent) {
        this.context = context;
        if (!context.docInfo().isMultipage()) {
            return indexPageContent;
        }

        Document jsoupDoc = Jsoup.parseBodyFragment(indexPageContent);
        List<Section> rootSections = sectionsOnLevel(jsoupDoc, jsoupDoc, 1);

        buildContentPages(rootSections);
        buildTocPage(jsoupDoc, rootSections);
        return buildIndexPage(jsoupDoc, rootSections);
    }

    private String buildIndexPage(Document jsoupDoc, List<Section> sections) {
        Elements preamble = jsoupDoc.select("#preamble");
        context.logger().info("peamble: " + preamble.text());
        StringBuilder sb = new StringBuilder(preamble.outerHtml()).append("\n");
        sectionListHtml(sections, sb);
        return sb.toString();
    }

    private void buildTocPage(Document jsoupDoc, List<Section> indexSections) {
        String fileName = context.docInfo().documentName() + ".toc.html";
        String existingToC = context.writer().getContent(fileName, null);
        if(existingToC == null) {
            StringBuilder sb = new StringBuilder("<div id=\"toc\" class=\"toc toc-side\">");
            buildTocLevel(sb, indexSections, 1);
            sb.append("</div>");
            context.writer().addContent(fileName, sb.toString());
        } else {
            Document existingToCDoc = Jsoup.parseBodyFragment(existingToC);
            Element firstChild = existingToCDoc.body().firstElementChild();
            if(firstChild == null) {
                context.logger().warn("Empty ToC content");
                return;
            }
            String toc = fixAnchors(jsoupDoc, firstChild, true);
            context.writer().addContent(fileName, toc);
        }
    }

    private void buildContentPages(List<Section> sections) {
        sections.forEach(s -> {
            context.writer().addContent(s.documentName(), context.docInfo().documentName(), s.content());
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

    private String fixAnchors(Element root, Element element, boolean addId) {
        element.select("a:not(.anchor)").forEach(el -> {
            String ref = el.attr("href");
            if (!ref.startsWith("#")) { // we are interested only in cross-docs references
                return;
            }

            String id = ref.substring(1);
            String refId = Section.normalizeId(id);
            Element sectionRoot = findSectionRoot(root, ref, context.docInfo().multipageLevel());
            if(sectionRoot != null) {
                String sectionRootId = Section.normalizeId(sectionRoot.id());
                if(!refId.equals(sectionRootId)) {
                    refId = sectionRootId + "#" + id;
                }
            }
            el.attr("href", ref(refId));
            if(addId) {
                el.id(refId);
            }
        });
        return element.outerHtml();
    }

    private List<Section> sectionsOnLevel(Element root, Element element, int currentLevel) {
        return element.select(".sect" + currentLevel).stream()
                .map(el -> {
                    Element sectionHeader = getSectionHeader(el, currentLevel);
                    if (sectionHeader == null) {
                        context.logger().warn("No header for a section " + el.children().iterator().next().outerHtml());
                        return null;
                    }
                    String sectionId = sectionHeader.id();
                    String title = el.child(0).text();
                    String header = header(title);
                    if (currentLevel < context.docInfo().multipageLevel()) {
                        List<Section> subsections = sectionsOnLevel(root, el, currentLevel + 1);
                        String content = subsections.isEmpty()
                                ? header + fixAnchors(root, el, false)
                                : buildIndexSection(header, title, subsections);
                        return new Section(sectionId, title, content, subsections);
                    } else {
                        return new Section(sectionId, title, header + fixAnchors(root, el, false));
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
        String content = context.docInfo().multipageHeader()
                .replaceAll("\\{title}", title);
        content = content.replaceAll("\\{weight}", Integer.toString(10 * weightCounter++));
        return content;
    }

    private String buildIndexSection(String header, String title, List<Section> subsections) {
        StringBuilder sb = new StringBuilder(header);
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
        return context.docInfo().multipageRef() + context.docInfo().documentName() + "/" + sectionId;
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
        context.logger().warn("Asciidoc multipage processor: No header for section " + target.id());
        return null;
    }

}
