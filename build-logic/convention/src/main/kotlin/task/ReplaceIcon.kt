/*
 * QAuxiliary - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2022 qwq233@qwq2333.top
 * https://github.com/cinit/QAuxiliary
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by QAuxiliary contributors.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/cinit/QAuxiliary/blob/master/LICENSE.md>.
 */

package task

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.random.Random

abstract class ReplaceIcon : Copy() {

    @get:Internal
    abstract val projectDir: DirectoryProperty
    @get:Input
    abstract var commitHash: String

    fun config() {
        into(File(projectDir.asFile.get(), "src/main/res/"))

        val iconsDir = File(projectDir.asFile.get(), "icons")
        val random = Random(commitHash.toSeed())
        // copy new icons
        val iconFileDirs = listOf(
            File(iconsDir, "QAPro"),
            File(iconsDir, "classic"),
            // File(iconsDir, "ChineseNewYearIcons")
        )
        val iconFile = iconFileDirs.flatMap { it.listFiles()!!.toList() }.random(random)
        println("Select Icon: $iconFile")
        into("drawable") {
            from(iconFile)
        }
        if (iconFile.isDirectory) {
            into("drawable-anydpi-v26") {
                from(File(iconsDir, "icon.xml"))
            }
        }
    }

    private fun String.toSeed(): Int {
        val md5 = MessageDigest.getInstance("MD5")
        val arrays = md5.digest(this.toByteArray(Charsets.UTF_8))
        return BigInteger(1, arrays).toInt()
    }
}
