/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package org.apache.cayenne.asciidoc.multipage;

import java.util.List;

public class Section implements Comparable<Section> {

    private final String sectionId;
    private final String title;
    private final String content;
    private final List<Section> subsections;

    public Section(String sectionId, String title, String content) {
        this.sectionId = sectionId;
        this.title = title;
        this.content = content;
        this.subsections = List.of();
    }

    public Section(String sectionId, String title, String content, List<Section> subsections) {
        this.sectionId = sectionId;
        this.title = title;
        this.content = content;
        this.subsections = subsections;
    }

    public String id() {
        return sectionId;
    }

    public String content() {
        return content;
    }

    public List<Section> subsections() {
        return subsections;
    }

    public String title() {
        return title;
    }

    public String documentName() {
        return sectionId + ".html";
    }

    @Override
    public int compareTo(Section o) {
        return title.compareTo(o.title());
    }
}
