# Changelog

## 0.1

* [X] 代码折叠：折叠标签文本为空的Markdown内联链接
  * 参见：`icu.windea.ut.toolbox.lang.md.EmptyMarkdownLinkFoldingBuilder`
* [X] 实现JAST，方面后续对类似JSON的文件格式统一提供扩展
  * 参见：`icu.windea.ut.toolbox.jast.JElementProvider`
  * 默认支持的文件格式：JSON、YAML
* [X] 实现（基于JSON指针的）语言设置，方面后续对类似JSON的文件格式统一提供扩展
  * 参见：`icu.windea.ut.toolbox.jast.JsonPointerBasedLanguageSettingsProvider`
* [X] 提供基于jsonSchema的（基于JSON指针的）语言设置，用于通过jsonSchema配置语言设置
  * 参见：`icu.windea.ut.toolbox.jsonSchema.JsonSchemaJsonPointerBasedLanguageSettingsProvider`
* [X] 提供预定义的jsonSchema，用于兼容基于jsonSchema的（基于JSON指针的）语言设置
  * 参见：`icu.windea.ut.toolbox.jsonSchema.UtJsonSchemaProviderFactory`
* [X] 提供基于（基于JSON指针的）语言设置的引用解析，以及相关的代码高亮、代码补全、快速文档、代码检查等语言功能
  * 参见：`icu.windea.ut.toolbox.jast.JsonPointerBasedReferenceProvider`
  * 参见：`icu.windea.ut.toolbox.jast.JsonPointerBasedReferenceUsagesSearcher`
  * 参见：`icu.windea.ut.toolbox.jast.JsonPointerBasedReferenceAnnotator`
  * 参见：`icu.windea.ut.toolbox.jast.JsonPointerBasedTargetElementEvaluator`
  * 参见：`icu.windea.ut.toolbox.jast.JsonPointerBasedReferenceCompletionProvider`
  * 参见：`icu.windea.ut.toolbox.jast.JsonPointerBasedDeclarationDocumentationTargetProvider`
  * 参见：`icu.windea.ut.toolbox.jast.UnresolvedJsonPointerBasedReferenceInspection`
* [ ] 提供扩展的折叠规则，适用于基于（基于JSON指针的）语言设置的声明/引用所在的对象或数组
* [ ] 扩展结构视图，适用于基于（基于JSON指针的）语言设置的声明/引用对应的属性或值