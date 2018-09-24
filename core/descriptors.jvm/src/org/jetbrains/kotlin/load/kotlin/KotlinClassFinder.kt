/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.load.kotlin

import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.serialization.deserialization.KotlinMetadataFinder

interface KotlinClassFinder : KotlinMetadataFinder {
    fun findKotlinClassOrClassFileContent(classId: ClassId): KotlinClassOrClassFileContent?

    fun findKotlinClassOrClassFileContent(javaClass: JavaClass): KotlinClassOrClassFileContent?

    fun findKotlinClass(classId: ClassId): KotlinJvmBinaryClass? =
        findKotlinClassOrClassFileContent(classId)?.toKotlinJvmBinaryClass()

    fun findKotlinClass(javaClass: JavaClass): KotlinJvmBinaryClass? =
        findKotlinClassOrClassFileContent(javaClass)?.toKotlinJvmBinaryClass()

    sealed class KotlinClassOrClassFileContent {
        fun toKotlinJvmBinaryClass(): KotlinJvmBinaryClass? = (this as? KotlinClass)?.KotlinJvmBinaryClass

        data class KotlinClass(val KotlinJvmBinaryClass: KotlinJvmBinaryClass) : KotlinClassOrClassFileContent()
        data class ClassFileContent(val content: ByteArray) : KotlinClassOrClassFileContent()
    }
}
