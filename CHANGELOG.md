# Changelog

## 1.0

* [X] 提供代码折叠规则，用于折叠标签文本为空的markdown内联链接
* [X] 实现JAST，用于后续对类似json的文件格式统一提供扩展（目前支持的文件格式：json、yaml）
* [X] 实现JAST的languageSchema，作为自定义的配置，用于后续为类似JSON的文件格式提供各种语言功能
* [X] 提供扩展，通过jsonSchema提供JAST的languageSchema
* [X] 基于JAST的languageSchema，提供引用解析、查找使用、代码高亮、代码补全、快速文档、代码检查等语言功能
* [X] 基于JAST的languageSchema指定的声明的容器的位置，提供扩展的代码折叠规则
* [X] 对于`.properties`文件，在本地化文本中换行时不自动缩进（可配置，覆盖IDE的默认实现）
* [X] 对于`.properties`文件，优化本地化属性的快速文档，显示处理后的本地化文本

***

* [X] Provides code folding rules for collapsing markdown inline links with empty label text
* [X] Implements the JAST, which is used to provide unified extensions for json-like file types (currently support: json, yaml)
* [X] Implements the language schema for JAST, as a custom configuration, it is used to provide various language features for json-like file types
* [X] Provides the extension to provide language schema based JAST via json schema
* [X] Provides language features including reference resolving, finding usages, code highlighting, code completion, quick documentation, code inspection, based on the language schema for JAST
* [X] Provides extended code folding rules, based on the language schema for JAST, which defines the locations of declaration containers
* [X] For `.properties` files, do not indent when enter in i18n text (configurable, override IDE's default implementation)
* [X] For `.properties` files, optimize the quick documentation for i18n properties, to show handled i18n text