package com.planwith.user.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.planwith.user", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule hexagonalLayers = layeredArchitecture()
            .consideringOnlyDependenciesInAnyPackage("com.planwith.user..")
            .layer("Domain").definedBy("com.planwith.user.domain..")
            .layer("Application").definedBy("com.planwith.user.application..")
            .layer("Adapter").definedBy("com.planwith.user.adapter..")
            .layer("Global").definedBy("com.planwith.user.global..")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapter", "Global")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapter", "Global")
            .whereLayer("Adapter").mayNotBeAccessedByAnyLayer()
            .whereLayer("Global").mayOnlyBeAccessedByLayers("Application", "Adapter");

    @ArchTest
    static final ArchRule domainMustNotDependOnFrameworks = noClasses()
            .that().resideInAPackage("com.planwith.user.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "jakarta.servlet..",
                    "org.hibernate..",
                    "org.springframework.data..",
                    "org.apache.kafka..",
                    "com.planwith.user.adapter..",
                    "com.planwith.user.global..",
                    "com.planwith.user.application.."
            );

    @ArchTest
    static final ArchRule applicationMustNotDependOnJpaOrAdapters = noClasses()
            .that().resideInAPackage("com.planwith.user.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.planwith.user.adapter..",
                    "org.springframework.data.jpa..",
                    "jakarta.persistence..",
                    "org.springframework.data.redis..",
                    "org.springframework.kafka.."
            );

    @ArchTest
    static final ArchRule applicationMustNotDependOnWeb = noClasses()
            .that().resideInAPackage("com.planwith.user.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.web..",
                    "jakarta.servlet.."
            );

    @ArchTest
    static final ArchRule controllersMustNotDependOnRepositories = noClasses()
            .that().resideInAPackage("com.planwith.user.adapter.in.web..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.planwith.user.adapter.out.persistence.repository..",
                    "org.springframework.data.jpa.."
            );

    @ArchTest
    static final ArchRule backendMustNotDependOnWebFlux = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.web.reactive..",
                    "reactor.core..",
                    "org.springframework.http.client.reactive.."
            );

    @ArchTest
    static final ArchRule noOauth2ResourceServer = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.security.oauth2.server.resource.."
            );
}
