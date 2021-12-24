/*
 * Copyright [2016] [Alexander Reelsen]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.elasticsearch.plugin.ingest.markdown;

import com.cybozu.labs.langdetect.SecureDetectorFactory;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.ingest.RandomDocumentPicks;
import org.elasticsearch.test.ESTestCase;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.FileInputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MarkdownProcessorTests extends ESTestCase {

    private static final String TEST_MARKDOWN_PATH = "render.md";

    @BeforeClass
    public static void loadProfiles() throws Exception {
        Settings settings = Settings.builder().put("path.home", createTempDir()).build();
        Environment environment = new Environment(settings, createTempDir());
        SecureDetectorFactory.loadProfileFromClassPath(environment);
    }

    public void testThatProcessorWorks() throws Exception {
        String directory = this.getClass().getResource("").getPath();
        FileInputStream fileInput = new FileInputStream(directory + TEST_MARKDOWN_PATH);
        byte[] bytes = IOUtils.toByteArray(fileInput);
        byte[] base64Bytes = Base64.getEncoder().encode(bytes);
        String value = new String(base64Bytes);
        Map<String, Object> config = this.config("source_field", "target_field", false);
        Map<String, Object> data = this.ingestDocument(config, "source_field", value);
        Map<String, Object> target_field = (Map<String, Object>) data.get("target_field");
        String content = (String) target_field.get("content");
        //验证目标字段去掉了markdown的标签
        Assert.assertTrue(!content.contains("**交互测试**"));
        Assert.assertTrue(content.contains("交互测试"));
    }

    private Map<String, Object> ingestDocument(Map<String, Object> config, String field, String value) throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put(field, value);
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), document);
        Processor processor = new MarkdownProcessor.Factory().create(Collections.emptyMap(), randomAlphaOfLength(10), "desc", config);
        return processor.execute(ingestDocument).getSourceAndMetadata();
    }

    private Map<String, Object> config(String sourceField, String targetField, boolean ignoreMissing) {
        final Map<String, Object> config = new HashMap<>();
        config.put("field", sourceField);
        config.put("target_field", targetField);
        config.put("ignore_missing", ignoreMissing);
        return config;
    }
}
