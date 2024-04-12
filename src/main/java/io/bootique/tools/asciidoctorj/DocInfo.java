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

import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;

import java.util.Collections;
import java.util.Map;

class DocInfo {

    private static final String EMPTY_FRONT_MATTER = "---\n---\n\n";
    private static final int MULTIPAGE_DEFAULT_LEVEL = 1;

    private static final String HEADER = "hugo-header";
    private static final String MULTIPAGE = "hugo-multipage";
    private static final String MULTIPAGE_LEVEL = "hugo-multipage-level";
    private static final String MULTIPAGE_HEADER = "hugo-multipage-header";
    private static final String MULTIPAGE_REF = "hugo-multipage-ref";
    private static final String FONT_AWESOME_ICONS = "hugo-font-awesome-icons";

    String documentName;
    String header;
    String multipageHeader;
    boolean multipage;
    int multipageLevel;
    String multipageRef;
    boolean convertToFa;

    @SuppressWarnings("unchecked")
    DocInfo(Document document, InternalLogger logger) {
        documentName = ((Map<String, ?>)document.getOptions().get(Options.ATTRIBUTES)).get("docname").toString();
        String headerFile = document.getAttribute(HEADER, "").toString();
        if(!headerFile.isEmpty()) {
            header = document.readAsset(headerFile, Collections.emptyMap());
            if(header == null) {
                logger.warn("Header file '" + headerFile + "' not found. Using a default header.");
                header = EMPTY_FRONT_MATTER;
            }
        } else {
            header = EMPTY_FRONT_MATTER;
        }
        multipage = Boolean.parseBoolean(document
                .getAttribute(MULTIPAGE, "false").toString());
        multipageLevel = Integer.parseInt(document
                .getAttribute(MULTIPAGE_LEVEL, MULTIPAGE_DEFAULT_LEVEL).toString());
        String multipageHeaderFile = document.getAttribute(MULTIPAGE_HEADER, "").toString();
        if(!multipageHeaderFile.isEmpty()) {
            multipageHeader = document.readAsset(multipageHeaderFile, Collections.emptyMap());
            if(multipageHeader == null) {
                logger.warn("Multipage header file '" + multipageHeaderFile + "' not found. Using a default header.");
                multipageHeader = EMPTY_FRONT_MATTER;
            }
        } else {
            multipageHeader = EMPTY_FRONT_MATTER;
        }
        multipageRef = document.getAttribute(MULTIPAGE_REF, "").toString();
        convertToFa = Boolean.parseBoolean(document
                .getAttribute(FONT_AWESOME_ICONS, "true").toString());
    }

    String documentName() {
        return documentName;
    }

    String header() {
        return header;
    }

    String multipageHeader() {
        return multipageHeader;
    }

    boolean isMultipage() {
        return multipage;
    }

    int multipageLevel() {
        return multipageLevel;
    }

    String multipageRef() {
        return multipageRef;
    }

    boolean convertToFa() {
        return convertToFa;
    }
}
