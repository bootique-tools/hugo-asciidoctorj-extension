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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ContentWriter {

    private final List<Content> generatedContent = new ArrayList<>(2);
    private final String destinationDir;
    private final InternalLogger logger;

    public ContentWriter(Document document, InternalLogger logger) {
        destinationDir = document.getOptions().get(Options.DESTINATION_DIR).toString();
        this.logger = logger;
    }

    public void addContent(String fileName, String content) {
        generatedContent.add(new Content(fileName, null, content));
    }

    public void addContent(String fileName, String folder, String content) {
        generatedContent.add(new Content(fileName, folder, content));
    }

    public void flush() {
        StandardOpenOption[] fileOptions = {
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
        };
        generatedContent.forEach(content -> {
            Path path = content.path(destinationDir);
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException ex) {
                logger.error(ex);
                throw new RuntimeException(ex);
            }
            try (BufferedWriter br = Files.newBufferedWriter(path, fileOptions)) {
                br.write(content.content, 0, content.content.length());
                br.flush();
            } catch (IOException ex) {
                logger.error(ex);
                throw new RuntimeException(ex);
            }
        });
    }

    static class Content {
        final String fileName;
        final String folder;
        final String content;

        Content(String fileName, String folder, String content) {
            this.fileName = fileName;
            this.folder = folder;
            this.content = content;
        }

        Path path(String destDir) {
            if(folder != null) {
                return Path.of(destDir, folder, fileName);
            } else {
                return Path.of(destDir, fileName);
            }
        }
    }

}
