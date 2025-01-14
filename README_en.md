# Utopian Toolbox

## Summary

A toolbox plugin integrates various extensions, mainly focus on the enhancement of IDE's support for file types like json, yaml and markdown.

## Feature List

* [X] Provides code folding rules for collapsing markdown inline links with empty label text
  * See: `icu.windea.ut.toolbox.lang.md.EmptyMarkdownLinkFoldingBuilder`
* [X] Implements the JAST, which is used to provide unified extensions for json-like file types (currently support: json, yaml)
  * See: `icu.windea.ut.toolbox.jast.JElementProvider`
* [X] Implements the language schema for JAST, as a custom configuration, it is used to provide various language features for json-like file types
  * See: `icu.windea.ut.toolbox.jast.LanguageSchemaProvider`
* [X] Provides the extension to provide language schema based JAST via json schema
  * See: `icu.windea.ut.toolbox.jsonSchema.JsonSchemaBasedLanguageSchemaProvider`
  * See: `icu.windea.ut.toolbox.jsonSchema.UtJsonSchemaProviderFactory`
  * Note: Restarting project may be required to apply modified configuration correctly
* [X] Provides language features including reference resolving, finding usages, code highlighting, code completion, quick documentation, code inspection, based on the language schema for JAST
  * See: `icu.windea.ut.toolbox.jast.LanguageSchemaBasedReferenceProvider`
  * See: `icu.windea.ut.toolbox.jast.LanguageSchemaBasedReferenceUsagesSearcher`
  * See: `icu.windea.ut.toolbox.jast.LanguageSchemaBasedReferenceAnnotator`
  * See: `icu.windea.ut.toolbox.jast.LanguageSchemaBasedTargetElementEvaluator`
  * See: `icu.windea.ut.toolbox.jast.LanguageSchemaBasedReferenceCompletionProvider`
  * See: `icu.windea.ut.toolbox.jast.LanguageSchemaBasedDeclarationDocumentationTargetProvider`
  * See: `icu.windea.ut.toolbox.jast.UnresolvedLanguageSchemaBasedReferenceInspection`
* [X] Provides extended code folding rules, based on the language schema for JAST, which defines the locations of declaration containers
  * See: `icu.windea.ut.toolbox.jast.LanguageSchemaBasedFoldingBuilder`
* [X] For `.properties` files, do not indent when enter in i18n text (configurable, override IDE's default implementation)
  * See: `icu.windea.ut.toolbox.lang.properties.EnterInPropertiesFileHandler`
* [X] For `.properties` files, optimize the quick documentation for i18n properties, to show handled i18n text
  * See: `icu.windea.ut.toolbox.lang.properties.PropertiesDocumentationProvider`