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

import java.util.Collections;
import java.util.List;

class Section implements Comparable<Section> {

    private final String sectionId;
    private final String title;
    private final String content;
    private final List<Section> subsections;

    Section(String sectionId, String title, String content) {
        this.sectionId = normalizeId(sectionId);
        this.title = title;
        this.content = content;
        this.subsections = Collections.emptyList();
    }

    Section(String sectionId, String title, String content, List<Section> subsections) {
        this.sectionId = normalizeId(sectionId);
        this.title = title;
        this.content = content;
        this.subsections = subsections;
    }

    String id() {
        return sectionId;
    }

    String content() {
        return content;
    }

    List<Section> subsections() {
        return subsections;
    }

    String title() {
        return title;
    }

    String documentName() {
        return sectionId + ".html";
    }

    @Override
    public int compareTo(Section o) {
        return title.compareTo(o.title());
    }

    static String normalizeId(String sectionId) {
        int idx = 0;
        while(sectionId.charAt(idx) == '_') {
            idx++;
        }
        if(idx != 0) {
            return sectionId.substring(idx);
        }
        return sectionId;
    }
}
