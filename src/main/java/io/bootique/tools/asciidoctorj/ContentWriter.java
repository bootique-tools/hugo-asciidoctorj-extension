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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class ContentWriter {

    private final List<Content> generatedContent = new ArrayList<>(2);
    private final String destinationDir;
    private final InternalLogger logger;

    ContentWriter(Document document, InternalLogger logger) {
        Object destDir = document.getOptions().get(Options.TO_DIR);
        if(destDir == null) {
            logger.error("No destination directory");
            throw new IllegalStateException("No destination directory found");
        }
        destinationDir = destDir.toString();
        this.logger = logger;
    }

    void addContent(String fileName, String content) {
        generatedContent.add(new Content(fileName, null, content));
    }

    void addContent(String fileName, String folder, String content) {
        generatedContent.add(new Content(fileName, folder, content));
    }

    String getContent(String fileName, String folder) {
        for(Content content : generatedContent) {
            if(Objects.equals(fileName, content.fileName) && Objects.equals(folder, content.folder)) {
                return content.content;
            }
        }
        return null;
    }

    void flush() {
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
