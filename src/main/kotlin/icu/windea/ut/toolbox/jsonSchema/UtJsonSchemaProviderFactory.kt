package icu.windea.ut.toolbox.jsonSchema

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.jetbrains.jsonSchema.extension.*
import com.jetbrains.jsonSchema.impl.*
import icu.windea.ut.toolbox.*
import org.jetbrains.annotations.*

class UtJsonSchemaProviderFactory : JsonSchemaProviderFactory, DumbAware {
    override fun getProviders(project: Project): List<JsonSchemaFileProvider> {
        return listOf(
            FileProvider(project, Info(JsonSchemaVersion.SCHEMA_7, "schema07.ut.json", "7", "http://json-schema.org/draft-07/schema"))
        )
    }

    data class Info(
        val version: JsonSchemaVersion,
        val bundledResourceFileName: String,
        val presentableSchemaId: @Nls String,
        val remoteSourceUrl: String
    )

    class FileProvider(private val project: Project, private val bundledSchema: Info) : JsonSchemaFileProvider {
        override fun isAvailable(file: VirtualFile): Boolean {
            return false
        }

        override fun getSchemaVersion(): JsonSchemaVersion {
            return bundledSchema.version
        }

        override fun getSchemaFile(): VirtualFile? {
            return JsonSchemaProviderFactory.getResourceFile(
                UtJsonSchemaProviderFactory::class.java,
                "/jsonSchema/${bundledSchema.bundledResourceFileName}"
            )
        }

        override fun getSchemaType(): SchemaType {
            return SchemaType.schema
        }

        override fun getRemoteSource(): String {
            return bundledSchema.remoteSourceUrl
        }

        override fun getPresentableName(): String {
            return UtBundle.message("jsonSchema.provider.name", bundledSchema.presentableSchemaId)
        }

        override fun getName(): String {
            return presentableName
        }
    }
}
