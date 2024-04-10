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

import org.asciidoctor.extension.Processor;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

public class InternalLogger {

    private final Processor processor;

    public InternalLogger(Processor processor) {
        this.processor = processor;
    }

    private void log(Severity severity, String message) {
        processor.log(new LogRecord(severity, message));
    }

    public void info(String message) {
        log(Severity.INFO, message);
    }

    public void warn(String message) {
        log(Severity.WARN, message);
    }

    public void error(Throwable th) {
        log(Severity.ERROR, th.getMessage());
        th.printStackTrace(System.err);
    }

    public void error(String message) {
        log(Severity.ERROR, message);
    }
}
