# Changelog

## 0.1

* [X] 代码折叠：折叠标签文本为空的Markdown内联链接
  * 参见：`icu.windea.ut.toolbox.lang.md.EmptyMarkdownLinkFoldingBuilder`
* [X] 实现JAST，用于后续对类似JSON的文件格式统一提供扩展
  * 参见：`icu.windea.ut.toolbox.jast.JElementProvider`
  * 默认支持的文件格式：JSON、YAML
* [X] 实现JAST的languageSchema，作为自定义的配置，用于后续为类似JSON的文件格式提供各种语言功能
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaProvider`
* [X] 提供扩展，通过jsonSchema提供JAST的languageSchema
  * 参见：`icu.windea.ut.toolbox.jsonSchema.JsonSchemaBasedLanguageSchemaProvider`
  * 参见：`icu.windea.ut.toolbox.jsonSchema.UtJsonSchemaProviderFactory`
* [X] 基于JAST的languageSchema，提供引用解析、查找使用、代码高亮、代码补全、快速文档、代码检查等语言功能
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaBasedReferenceProvider`
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaBasedReferenceUsagesSearcher`
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaBasedReferenceAnnotator`
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaBasedTargetElementEvaluator`
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaBasedReferenceCompletionProvider`
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaBasedDeclarationDocumentationTargetProvider`
  * 参见：`icu.windea.ut.toolbox.jast.UnresolvedLanguageSchemaBasedReferenceInspection`
* [ ] 基于由JAST的languageSchema定义的声明/引用的位置，提供扩展的代码折叠规则