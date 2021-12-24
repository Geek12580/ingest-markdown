package org.elasticsearch.plugin.ingest.markdown;

import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.SecureDetectorFactory;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.plugins.Plugin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * 从markdown文件中提取纯文本信息的插件
 * @author zengshenw
 */
public class MarkdownPlugin extends Plugin implements IngestPlugin {

    @Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
        try {
            SecureDetectorFactory.loadProfileFromClassPath(parameters.env);
        }
        catch (LangDetectException | URISyntaxException | IOException e) {
            throw new ElasticsearchException(e);
        }
        Map<String, Processor.Factory> factoryMap = new HashMap<>(1);
        factoryMap.put(MarkdownProcessor.TYPE, new MarkdownProcessor.Factory());
        return factoryMap;
    }
}
