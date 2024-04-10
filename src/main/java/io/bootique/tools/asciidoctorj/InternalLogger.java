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

import org.asciidoctor.extension.Processor;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

class InternalLogger {

    private final Processor processor;

    InternalLogger(Processor processor) {
        this.processor = processor;
    }

    private void log(Severity severity, String message) {
        processor.log(new LogRecord(severity, message));
    }

    void info(String message) {
        log(Severity.INFO, message);
    }

    void warn(String message) {
        log(Severity.WARN, message);
    }

    void error(Throwable th) {
        log(Severity.ERROR, th.getMessage());
        th.printStackTrace(System.err);
    }

    void error(String message) {
        log(Severity.ERROR, message);
    }
}
