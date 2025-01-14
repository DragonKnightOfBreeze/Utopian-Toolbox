# Utopian Toolbox

## 概述

汇总了各种扩展的工具插件，主要用于增强IDE对json、yaml、markdown等文件格式的支持。

## 功能列表

* [X] 提供代码折叠规则，用于折叠标签文本为空的Markdown内联链接
  * 参见：`icu.windea.ut.toolbox.lang.md.EmptyMarkdownLinkFoldingBuilder`
* [X] 实现JAST，用于后续对类似json的文件格式统一提供扩展（目前支持的文件格式：json、yaml）
  * 参见：`icu.windea.ut.toolbox.jast.JElementProvider`
* [X] 实现JAST的languageSchema，作为自定义的配置，用于后续为类似JSON的文件格式提供各种语言功能
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaProvider`
* [X] 提供扩展，通过jsonSchema提供JAST的languageSchema
  * 参见：`icu.windea.ut.toolbox.jsonSchema.JsonSchemaBasedLanguageSchemaProvider`
  * 参见：`icu.windea.ut.toolbox.jsonSchema.UtJsonSchemaProviderFactory`
  * 备注：可能需要重启项目才能正确应用更改后的配置
* [X] 基于JAST的languageSchema，提供引用解析、查找使用、代码高亮、代码补全、快速文档、代码检查等语言功能
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaBasedReferenceProvider`
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaBasedReferenceUsagesSearcher`
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaBasedReferenceAnnotator`
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaBasedTargetElementEvaluator`
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaBasedReferenceCompletionProvider`
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaBasedDeclarationDocumentationTargetProvider`
  * 参见：`icu.windea.ut.toolbox.jast.UnresolvedLanguageSchemaBasedReferenceInspection`
* [X] 基于JAST的languageSchema指定的声明的容器的位置，提供扩展的代码折叠规则
  * 参见：`icu.windea.ut.toolbox.jast.LanguageSchemaBasedFoldingBuilder`
* [X] 对于`.properties`文件，在本地化文本中换行时不自动缩进（可配置，覆盖IDE的默认实现）
  * 参见：`icu.windea.ut.toolbox.lang.properties.EnterInPropertiesFileHandler`
* [X] 对于`.properties`文件，优化本地化属性的快速文档，显示处理后的本地化文本
  * 参见：`icu.windea.ut.toolbox.lang.properties.PropertiesDocumentationProvider`