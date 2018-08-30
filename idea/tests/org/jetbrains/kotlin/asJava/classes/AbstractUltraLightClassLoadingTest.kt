/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava.classes

import org.jetbrains.kotlin.idea.perf.UltraLightChecker
import org.jetbrains.kotlin.idea.test.KotlinLightCodeInsightFixtureTestCase
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

abstract class AbstractUltraLightClassLoadingTest : KotlinLightCodeInsightFixtureTestCase() {
    fun doTest(testDataPath: String) {
        val file = myFixture.addFileToProject(testDataPath, File(testDataPath).readText()) as KtFile
        for (ktClass in file.declarations.filterIsInstance<KtClassOrObject>().toList()) {
            val clsLoadingExpected = ktClass.docComment?.text?.contains("should load cls") == true
            val ultraLightClass = UltraLightChecker.checkClassEquivalence(ktClass)!!
            assertEquals(
                "Cls-loaded status differs from expected for ${ultraLightClass.qualifiedName}",
                clsLoadingExpected,
                ultraLightClass.isClsDelegateLoaded
            )
        }

    }
}