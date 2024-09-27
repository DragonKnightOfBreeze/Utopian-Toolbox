# Changelog

## 0.1

* [X] 代码折叠：折叠标签文本为空的Markdown内联链接
  * 参见：`icu.windea.ut.toolbox.lang.md.EmptyMarkdownLinkFoldingBuilder`
* [X] 实现JAST，方面后续对类似JSON的文件格式统一提供扩展
  * 参见：`icu.windea.ut.toolbox.jast.JElementProvider`
  * 默认支持的文件格式：JSON、YAML
* [X] 扩展jsonSchema，提供基于ANT路径表达式与JSON指针的引用解析
  * 参见：`icu.windea.ut.toolbox.jast.JsonPointerBasedLanguageSettingsProvider` 
  * 参见：`icu.windea.ut.toolbox.jsonSchema.JsonSchemaJsonPointerBasedLanguageSettingsProvider` 
  * 参见：`icu.windea.ut.toolbox.jast.JsonPointerBasedReferenceProvider`
  * 默认支持的文件格式：JSON、YAML
