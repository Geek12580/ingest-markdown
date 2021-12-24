package org.elasticsearch.plugin.ingest.markdown;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.elasticsearch.ingest.ConfigurationUtils.readBooleanProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

/**
 * 用于从markdown文件中提取纯文本的处理器
 *
 * @author zengshenw
 */
public class MarkdownProcessor extends AbstractProcessor {

    public static final String TYPE = "markdown";

    private static final Parser PARSER = Parser.builder().build();

    private static final TextContentRenderer RENDERER = TextContentRenderer.builder().build();

    private final String field;

    private final String targetField;

    private final boolean ignoreMissing;

    private final boolean removeBinary;

    public MarkdownProcessor(String tag, String description, String field, String targetField, boolean ignoreMissing, boolean removeBinary) {
        super(tag, description);
        this.field = field;
        this.targetField = targetField;
        this.ignoreMissing = ignoreMissing;
        this.removeBinary = removeBinary;
    }

    @Override
    public IngestDocument execute(IngestDocument ingestDocument) {
        byte[] input = ingestDocument.getFieldValueAsBytes(field, ignoreMissing);
        if (input == null && ignoreMissing) {
            return ingestDocument;
        }
        else if (input == null) {
            throw new IllegalArgumentException("field [" + field + "] is null, cannot parse.");
        }
        String parsedContent;
        try {
            Node document = PARSER.parse(new String(input));
            parsedContent = RENDERER.render(document);
        }
        catch (Exception e) {
            throw new ElasticsearchParseException("Error parsing document in field [{}]", e, field);
        }

        Map<String, Object> additionalFields = new HashMap<>(4);
        additionalFields.put(Property.CONTENT.toLowerCase(), parsedContent.trim());
        additionalFields.put(Property.CONTENT_TYPE.toLowerCase(), "text/markdown");
        additionalFields.put(Property.CONTENT_LENGTH.toLowerCase(), parsedContent.length());

        ingestDocument.setFieldValue(targetField, additionalFields);

        if (removeBinary) {
            ingestDocument.removeField(field);
        }
        return ingestDocument;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {

        @Override
        public Processor create(Map<String, Processor.Factory> processorFactories, String tag, String description, Map<String, Object> config) {
            String field = readStringProperty(TYPE, tag, config, "field");
            String targetField = readStringProperty(TYPE, tag, config, "target_field");
            boolean removeBinary = readBooleanProperty(TYPE, tag, config, "remove_binary", false);
            boolean ignoreMissing = readBooleanProperty(TYPE, tag, config, "ignore_missing", false);
            return new MarkdownProcessor(tag, description, field, targetField, ignoreMissing, removeBinary);
        }
    }

    /**
     * 解析markdown后生成的map用到的属性
     */
    enum Property {
        /**
         * 纯文本内容
         */
        CONTENT,
        /**
         * 解析的文本的类型，对于本插件而言是markdown
         */
        CONTENT_TYPE,
        /**
         * 解析后文本的长度
         */
        CONTENT_LENGTH;

        public String toLowerCase() {
            return this.toString().toLowerCase(Locale.ROOT);
        }
    }
}
