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

package org.apache.cayenne.asciidoc;

import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;

import java.util.Collections;
import java.util.Map;

public class DocInfo {

    private static final String EMPTY_FRONT_MATTER = "---\n---\n\n";
    private static final int MULTIPAGE_DEFAULT_LEVEL = 1;

    String documentName;

    String header;

    String multipageHeader;

    boolean multipage;

    int multipageLevel;

    String multipageRef;

    @SuppressWarnings("unchecked")
    public DocInfo(Document document) {
        documentName = ((Map<String, ?>)document.getOptions().get(Options.ATTRIBUTES)).get("docname").toString();
        String headerFile = document.getAttribute("cayenne-header", "").toString();
        if(!headerFile.isEmpty()) {
            header = document.readAsset(headerFile, Collections.emptyMap());
        } else {
            header = EMPTY_FRONT_MATTER;
        }
        multipage = Boolean.parseBoolean(document
                .getAttribute("multipage", "false").toString());
        multipageLevel = Integer.parseInt(document
                .getAttribute("multipage-level", MULTIPAGE_DEFAULT_LEVEL).toString());
        String multipageHeaderFile = document.getAttribute("multipage-header", "").toString();
        if(!multipageHeaderFile.isEmpty()) {
            multipageHeader = document.readAsset(multipageHeaderFile, Collections.emptyMap());
        } else {
            multipageHeader = EMPTY_FRONT_MATTER;
        }
        multipageRef = document.getAttribute("multipage-ref", "").toString();
    }

    public String documentName() {
        return documentName;
    }

    public String header() {
        return header;
    }

    public String multipageHeader() {
        return multipageHeader;
    }

    public boolean isMultipage() {
        return multipage;
    }

    public int multipageLevel() {
        return multipageLevel;
    }

    public String multipageRef() {
        return multipageRef;
    }
}
