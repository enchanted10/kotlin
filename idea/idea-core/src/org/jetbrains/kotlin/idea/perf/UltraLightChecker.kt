/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.perf

import com.intellij.psi.*
import org.jetbrains.annotations.TestOnly
import org.jetbrains.kotlin.asJava.classes.KtLightClassForSourceDeclaration
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.junit.Assert

@TestOnly
object UltraLightChecker {
    fun checkClassEquivalence(file: KtFile) {
        for (ktClass in file.declarations.filterIsInstance<KtClassOrObject>().toList()) {
            checkClassEquivalence(ktClass)
        }
    }

    fun checkClassEquivalence(ktClass: KtClassOrObject): KtLightClassForSourceDeclaration? {
        val gold = KtLightClassForSourceDeclaration.create(ktClass)
        val ultraLightClass = KtLightClassForSourceDeclaration.createUltraLight(ktClass)
        if (gold != null) {
            Assert.assertFalse(gold.javaClass.name.contains("Ultra"))
        }

        val goldText = gold?.render().orEmpty()
        val ultraText = ultraLightClass?.render().orEmpty()

        if (goldText != ultraText) {
            Assert.assertEquals(
                "//Classic implementation:\n$goldText",
                "//Light implementation:\n$ultraText"
            )
        }
        return ultraLightClass
    }

    private fun PsiClass.render(): String {
        fun PsiAnnotation.renderAnnotation() =
            "@" + qualifiedName + "(" + parameterList.attributes.joinToString { it.name + "=" + (it.value?.text ?: "?") } + ")"

        fun PsiModifierListOwner.renderModifiers() =
            annotations.joinToString("") { it.renderAnnotation() + (if (this is PsiParameter) " " else "\n") } +
                    PsiModifier.MODIFIERS.filter(::hasModifierProperty).joinToString("") { "$it " }

        fun PsiType.renderType() = getCanonicalText(true)

        fun PsiReferenceList?.renderRefList(keyword: String): String {
            if (this == null || this.referencedTypes.isEmpty()) return ""
            return " " + keyword + " " + referencedTypes.joinToString { it.renderType() }
        }

        fun PsiVariable.renderVar(): String {
            var result = this.renderModifiers() + type.renderType() + " " + name
            if (this is PsiParameter && this.isVarArgs) {
                result += " /* vararg */"
            }
            computeConstantValue()?.let { result += " /* constant value $it */" }
            return result
        }

        fun PsiTypeParameterListOwner.renderTypeParams() =
            if (typeParameters.isEmpty()) ""
            else "<" + typeParameters.joinToString {
                val bounds =
                    if (it.extendsListTypes.isNotEmpty())
                        " extends " + it.extendsListTypes.joinToString(" & ", transform = PsiClassType::renderType)
                    else ""
                it.name!! + bounds
            } + "> "

        fun PsiMethod.renderMethod() =
            renderModifiers() +
                    (if (isVarArgs) "/* vararg */ " else "") +
                    renderTypeParams() +
                    (returnType?.renderType() ?: "") + " " +
                    name +
                    "(" + parameterList.parameters.joinToString { it.renderModifiers() + it.type.renderType() } + ")" +
                    (this as? PsiAnnotationMethod)?.defaultValue?.let { " default " + it.text }.orEmpty() +
                    throwsList.referencedTypes.let { thrownTypes ->
                        if (thrownTypes.isEmpty()) ""
                        else " throws " + thrownTypes.joinToString { it.renderType() }
                    } +
                    ";"

        val classWord = when {
            isAnnotationType -> "@interface"
            isInterface -> "interface"
            isEnum -> "enum"
            else -> "class"
        }

        return renderModifiers() +
                classWord + " " +
                name + " /* " + qualifiedName + "*/" +
                renderTypeParams() +
                extendsList.renderRefList("extends") +
                implementsList.renderRefList("implements") +
                " {\n" +
                (if (isEnum) fields.filterIsInstance<PsiEnumConstant>().joinToString(",\n") { it.name } + ";\n\n" else "") +
                fields.filterNot { it is PsiEnumConstant }.map { it.renderVar().prependIndent("  ") + ";\n\n" }.sorted().joinToString("") +
                methods.map { it.renderMethod().prependIndent("  ") + "\n\n" }.sorted().joinToString("") +
                innerClasses.map { it.render().prependIndent("  ") }.sorted().joinToString("") +
                "}"
    }

}