# Changelog

## 0.1

* [X] 提供代码折叠规则，用于折叠标签文本为空的markdown内联链接
* [X] 实现JAST，用于后续对类似json的文件格式统一提供扩展（目前支持的文件格式：json、yaml）
* [X] 实现JAST的languageSchema，作为自定义的配置，用于后续为类似JSON的文件格式提供各种语言功能
* [X] 提供扩展，通过jsonSchema提供JAST的languageSchema
* [X] 基于JAST的languageSchema，提供引用解析、查找使用、代码高亮、代码补全、快速文档、代码检查等语言功能
* [X] 基于JAST的languageSchema指定的声明的容器的位置，提供扩展的代码折叠规则