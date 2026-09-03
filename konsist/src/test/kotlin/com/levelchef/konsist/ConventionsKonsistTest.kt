package com.levelchef.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

/** Guards the naming / packaging conventions from AGENTS.md. */
class ConventionsKonsistTest {

    private val scope = Konsist.scopeFromProject()

    @Test
    fun every_file_has_a_package_under_com_levelchef() {
        scope.files
            .filter { it.path.contains("/src/") && !it.path.contains("/konsist/") }
            .assertTrue { it.packagee?.name?.startsWith("com.levelchef") == true }
    }

    @Test
    fun test_functions_use_snake_case_without_backticks() {
        val snakeCase = Regex("[a-z][a-z0-9_]*")
        scope.functions()
            .filter { fn -> fn.annotations.any { it.name == "Test" } }
            .assertTrue { snakeCase.matches(it.name) }
    }

    @Test
    fun view_model_classes_reside_in_a_feature_package() {
        scope.classes()
            .filter { it.name.endsWith("ViewModel") }
            .assertTrue { it.resideInPackage("com.levelchef.feature..") }
    }

    @Test
    fun repository_interfaces_live_in_domain_and_implementations_in_data() {
        scope.interfaces()
            .filter { it.name.endsWith("Repository") }
            .assertTrue { it.resideInPackage("com.levelchef.domain..") }

        scope.classes()
            .filter { it.name.endsWith("RepositoryImpl") }
            .assertTrue { it.resideInPackage("com.levelchef.data..") }
    }

    @Test
    fun core_model_files_declare_a_single_public_top_level_type() {
        scope.files
            .filter { it.packagee?.name?.startsWith("com.levelchef.core.model") == true }
            .assertTrue { file ->
                val publicTopLevelTypes = (file.classes() + file.interfaces() + file.objects())
                    .filter { it.isTopLevel }
                    .count { it.hasPublicOrDefaultModifier }
                publicTopLevelTypes <= 1
            }
    }
}
