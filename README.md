# Elasticsearch Markdown Ingest Processor

## 项目介绍
Elasticsearch的Ingest Processor将待写入的数据传入到pipeline，再通过pipeline配置的processor list对数据进行预处理。
其中[Ingest Attachment plugin](https://www.elastic.co/guide/en/elasticsearch/plugins/7.12/ingest-attachment.html) ,用于分离附件内容的元数据信息和纯文本信息， 
Ingest attachment plugin基于apache提供的tika库实现，而tika不支持识别markdown格式的文本。  

本项目旨在于拓展ingest attachment plugin的功能，支持识别markdown格式的文本，基于开源仓库[commonmark](https://github.com/commonmark/commonmark-java) 实现。

## 快速开始

### 配置安装
1. 在[常见版本插件包](#常见版本插件包)列表中找到当前正在使用的Elasticsearch版本对应的插件包，如未找到，参考[自定义生成插件包](#自定义生成插件包)生成对应版本的插件包。
2. 下载插件包后上传到Elasticsearch服务器，路径不限，这里假定路径为path/to/plugin.zip。
3. 通过命令行进入到Elasticsearch安装的根目录，安装插件。

```bash
./bin/elasticsearch-plugin install file:///path/to/plugin.zip
```

安装好插件后可以通过如下命令查看插件是否安装好了。

```bash
./bin/elasticsearch-plugin list
```

4. 重新启动服务器，**Elasticsearch集群环境下，需要在每个节点都安装插件。**

### 使用方法
1. 创建一个用于解析markdown的pipeline，processors列表带上自定义实现的插件名称，本插件的名称是`markdown`， 指定markdown原文字段和存放解析为纯文本的目标字段。
下面的例子还额外增加了一个remove插件，用于删除markdown原文字段，如果需要保留原文字段，定义pipeline时不定义remove插件。
```http request
PUT _ingest/pipeline/markdown-pipeline
{
    "description": "A pipeline to extract plain text from markdown",
    "processors": [
        {
            "markdown": {
                "field": "markdownField",
                "target_field": "markdownField_doc"
            }
        },
        {
            "remove": {
                "field": "markdownField"
            }
        }
    ]
}
```

2. 通过rest api将数据写入到Elasticsearch时指定下定义的pipeline，一般的put或者bulk写入都支持，注意传入的数据需要经过Base64编码。
```http request
PUT /my-index/my-type/1?pipeline=markdown-pipeline
{
  "markdownField" : "IyDns7vnu5/liY3lkI7nq6/liqDlr4blrp7njrAKCiMjIOmcgOaxggoK5Zyo5L2/55Soc3VjY2Jp55qE5pe25YCZ77yM5pyJ55qE5LyB5Lia6ZyA6KaB5aSW572R6IO95aSf6K6/6Zeu77yM6L+Z5bCx6ZyA6KaB5YGa5a6J5YWo6K+E5rWL77yM5Lya5a+55YmN56uv6K+35rGC5ZKM5ZCO56uv6L+U5Zue55qE5pWP5oSf5L+h5oGv5qOA5rWL44CCCui/meS4quaYr+avlOi+g+mAmueUqOeahOmcgOaxguOAguiAg+iZkeWcqOezu+e7n+WxgumdouWOu+aPkOS+m+mAmueUqOeahOacuuWItuWOu+WunueOsOi/meS4quWKn+iDvQoKIyMg5Yqf6IO954K5CgoxLiDliY3nq6/or7fmsYJib2R55Yqg5a+G77yM5ZCO56uv5a+55YW26Kej5a+G44CC5YmN56uv5Lyg5YWl55qEYGNvbnRlbnRUeXBlYOaYr2BhcHBsaWNhdGlvbi9qc29uYO+8jOW5tuW4puS4iuiHquWumuS5ieivt+axguWktGBYLUVuY3J5cHQtUmVxdWVzdGDjgIIK5ZCO56uv55qEYEh0dHBNZXNzYWdlQ29udmVydGVyYOS8muWIpOaWreezu+e7n+eahOiuvue9ruWSjOivt+axguaYr+WQpuW4puS4imBYLUVuY3J5cHQtUmVxdWVzdGDvvIzmu6HotrPmnaHku7bnmoTkvJrlr7nor7fmsYLop6Plr4bjgIIKMi4g5ZCO56uv6L+U5Zue55qE5byC5bi45L+h5oGv5a+55byC5bi46L+b6KGM5Yqg5a+G5aSE55CG77yM5YmN56uv5a+55YW26L+b6KGM6Kej5a+GCgojIyDlhbfkvZPlrp7njrAKCjEuIOaPkOS+m+ezu+e7n+e6p+WIq+eahOiuvue9ru+8jOiuvue9rueahOmUruaYr2BzZWN1cml0eS5wcm90ZWN0aW9uLmVuY3J5cHRBamF4RGF0YWDjgILooajnpLrns7vnu5/mmK/lkKblr7nor7fmsYLkvZPliqDlr4bjgIIKMi4g6K6+572uYHNlY3VyaXR5LnByb3RlY3Rpb24uZW5jcnlwdEFqYXhEYXRhYO+8jOWJjeerr+Wwhuivt+axguS9k+WFiOaxgmJhc2U2NO+8jOWGjeS9v+eUqGx6LXN0cmluZ+WKoOWvhu+8jOi/meagt+WBmueahOWOn+WboOaYr++8jOWPkeeOsOacieaXtuWAmeebtOaOpeWKoOWvhuS6huWQjuerr+ino+aekOS4jeWHuuadpeOAggozLiDorr7nva5gc2VjdXJpdHkucHJvdGVjdGlvbi5lbmNyeXB0QWpheERhdGFg77yM5ZCO56uv5a6e546w5LqG6Ieq5a6a5LmJYEh0dHBNZXNzYWdlQ29udmVydGVyYO+8mmBFbmNyeXB0SHR0cE1lc3NhZ2VDb252ZXJ0ZXJg55So5LqO6Kej5p6Q55u45YWz5YaF5a6544CCCjQuIOWFs+S6juW8guW4uOWkhOeQhu+8muW8guW4uOS9v+eUqGJhc2U2NOWKoOWvhuW8guW4uOWghuagiOWwseihjOS6hu+8jOWJjeerr+WwhuWug+ino+aekOWHuuadpeOAguW5tuS4lOW8guW4uOaYr+avj+S4qumDveWKoOeahOOAggo="
}
```

3. 写入完成后，可以执行下面的请求验证下解析结果。
```http request
GET /my-index/my-type/1

# Expected response
{
  "_index" : "my-index",
  "_type" : "my-type",
  "_id" : "1",
  "_version" : 1,
  "_seq_no" : 0,
  "_primary_term" : 1,
  "_ignored" : [
    "markdownField_doc.content.keyword"
  ],
  "found" : true,
  "_source" : {
    "markdownField_doc" : {
      "content_type" : "text/markdown",
      "content" : """系统前后端加密实现
需求
在使用succbi的时候，有的企业需要外网能够访问，这就需要做安全评测，会对前端请求和后端返回的敏感信息检测。
这个是比较通用的需求。考虑在系统层面去提供通用的机制去实现这个功能
功能点
1. 前端请求body加密，后端对其解密。前端传入的"contentType"是"application/json"，并带上自定义请求头"X-Encrypt-Request"。
后端的"HttpMessageConverter"会判断系统的设置和请求是否带上"X-Encrypt-Request"，满足条件的会对请求解密。
2. 后端返回的异常信息对异常进行加密处理，前端对其进行解密
具体实现
1. 提供系统级别的设置，设置的键是"security.protection.encryptAjaxData"。表示系统是否对请求体加密。
2. 设置"security.protection.encryptAjaxData"，前端将请求体先求base64，再使用lz-string加密，这样做的原因是，发现有时候直接加密了后端解析不出来。
3. 设置"security.protection.encryptAjaxData"，后端实现了自定义"HttpMessageConverter"："EncryptHttpMessageConverter"用于解析相关内容。
4. 关于异常处理：异常使用base64加密异常堆栈就行了，前端将它解析出来。并且异常是每个都加的。""",
      "content_length" : 638
    }
  }
}
```

### Elasticsearch模型使用插件配置

数据模块的Elasticsearch模型如若需要使用本插件提取markdown文件的纯文本信息，需要在模型字段上做如下设置。
1. 设置模型字段的**字段角色**为markdown。
2. 设置模型字段**ES附件处理插件名称**为自定义插件的名称，本项目插件的名称为`markdown`，默认为`attachment`。

## 自定义生成插件包

在[常见版本插件包](#常见版本插件包)列表中未找到对应版本的插件包时，可以遵循以下步骤自行生成插件包。
1. 导入本仓库到ide中，定位到项目根目录下的**gradle.properties**文件。
2. 修改**elasticsearchVersion**配置项，修改为需要适配的版本。
3. 基于gradle打包，可执行如下命令，生成对应的zip文件。

```bash
gradle assemble
```

zip文件位于**build/distributions** 目录下。

## 常见版本插件包

| ES     | location                                                   |
|--------|------------------------------------------------------------|
| 7.16.1 | `build/distribution/ingest-markdown-7.16.1.1-SNAPSHOT.zip` |
| 7.16.0 | `build/distribution/ingest-markdown-7.16.0.1-SNAPSHOT.zip` |
| 7.15.1 | `build/distribution/ingest-markdown-7.15.1.1-SNAPSHOT.zip` |
| 7.15.0 | `build/distribution/ingest-markdown-7.15.0.1-SNAPSHOT.zip` |
| 7.12.1 | `build/distribution/ingest-markdown-7.12.1.1-SNAPSHOT.zip` |
| 7.12.0 | `build/distribution/ingest-markdown-7.12.0.1-SNAPSHOT.zip` |