# Changelog

## 0.1

* [X] 代码折叠：折叠标签文本为空的Markdown内联链接
  * 参见：`icu.windea.ut.toolbox.lang.md.EmptyMarkdownLinkFoldingBuilder`
* [X] 实现JAST，方面后续对类似JSON的文件格式统一提供扩展
  * 参见：`icu.windea.ut.toolbox.jast.JElementProvider`
  * 默认支持的文件格式：JSON、YAML
* [X] 扩展jsonSchema，提供（基于JSON指针的）语言设置，方面后续对类似JSON的文件格式统一提供扩展
  * 参见：`icu.windea.ut.toolbox.jast.JsonPointerBasedLanguageSettingsProvider` 
  * 参见：`icu.windea.ut.toolbox.jsonSchema.JsonSchemaJsonPointerBasedLanguageSettingsProvider`
* [X] 提供基于（基于JSON指针的）语言设置的引用解析，以及相关的代码高亮、代码检查、代码补全、快速文档等语言功能
  * 参见：`icu.windea.ut.toolbox.jast.JsonPointerBasedReferenceProvider`
  * 参见：`icu.windea.ut.toolbox.jast.JsonPointerBasedReferenceUsagesSearcher`
  * 参见：`icu.windea.ut.toolbox.jast.JsonPointerBasedReferenceAnnotator`
  * 参见：`icu.windea.ut.toolbox.jast.UnresolvedJsonPointerBasedReferenceInspection`
