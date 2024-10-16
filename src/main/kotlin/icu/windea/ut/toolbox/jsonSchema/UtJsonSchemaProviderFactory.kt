package icu.windea.ut.toolbox.jsonSchema

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.jetbrains.jsonSchema.extension.*
import com.jetbrains.jsonSchema.ide.*
import com.jetbrains.jsonSchema.impl.*
import icu.windea.ut.toolbox.*
import org.jetbrains.annotations.*

class UtJsonSchemaProviderFactory : JsonSchemaProviderFactory, DumbAware {
    override fun getProviders(project: Project): List<JsonSchemaFileProvider> {
        return listOf(
            FileProvider(project, Info(JsonSchemaVersion.SCHEMA_7, "schema07.ut.json", "7"))
        )
    }

    data class Info(
        val version: JsonSchemaVersion,
        val bundledResourceFileName: String,
        val presentableSchemaId: @Nls String
    )

    class FileProvider(private val project: Project, private val bundledSchema: Info) : JsonSchemaFileProvider {
        override fun isAvailable(file: VirtualFile): Boolean {
            if (project.isDisposed) return false
            val service = JsonSchemaService.Impl.get(project)
            if (!service.isApplicableToFile(file)) return false
            val instanceSchemaVersion = service.getSchemaVersion(file)
            if (instanceSchemaVersion == null) return false
            return instanceSchemaVersion == bundledSchema.version
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

        override fun getRemoteSource(): String? {
            return "https://github.com/DragonKnightOfBreeze/Utopian-Toolbox/blob/master/src/main/resources/jsonSchema/" + bundledSchema.bundledResourceFileName
        }

        override fun getPresentableName(): String {
            return UtBundle.message("jsonSchema.provider.name", bundledSchema.presentableSchemaId)
        }

        override fun getName(): String {
            return presentableName
        }
    }
}
