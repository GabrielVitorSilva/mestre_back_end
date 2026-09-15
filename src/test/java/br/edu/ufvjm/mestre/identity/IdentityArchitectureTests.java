package br.edu.ufvjm.mestre.identity;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityArchitectureTests {
    private static final JavaClasses CODE = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("br.edu.ufvjm.mestre.identity");

    @Test
    void allExpectedBoundariesActuallyExist() {
        for (String boundary : new String[]{
                "domain.account", "domain.registration", "application.account", "application.password",
                "application.registration", "adapter.http.registration", "adapter.persistence.account",
                "adapter.password", "configuration"}) {
            String packageName = "br.edu.ufvjm.mestre.identity." + boundary;
            assertTrue(CODE.stream().anyMatch(c -> c.getPackageName().equals(packageName)
                    || c.getPackageName().startsWith(packageName + ".")), boundary);
        }
    }
    @Test
    void coreDependenciesPointInwards() {
        classes().that().resideInAPackage("..identity.domain..")
                .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "..identity.domain..")
                .allowEmptyShould(false).check(CODE);
        classes().that().resideInAPackage("..identity.application..")
                .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "..identity.domain..", "..identity.application..")
                .allowEmptyShould(false).check(CODE);
    }
    @Test
    void adaptersDoNotDependOnEachOtherOrComposition() {
        for (String adapter : new String[]{"http", "persistence", "password"}) {
            for (String other : new String[]{"http", "persistence", "password", "configuration"}) {
                if (adapter.equals(other)) continue;
                String target = other.equals("configuration") ? "..identity.configuration.." : "..identity.adapter." + other + "..";
                noClasses().that().resideInAPackage("..identity.adapter." + adapter + "..")
                        .should().dependOnClassesThat().resideInAPackage(target)
                        .allowEmptyShould(false).check(CODE);
            }
        }
    }
}
