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

package io.github.qauxv.hook

import io.github.qauxv.base.IDynamicHook
import io.github.qauxv.base.RuntimeErrorTracer
import io.github.qauxv.step.DexDeobfStep
import io.github.qauxv.step.Step
import io.github.qauxv.util.Log
import io.github.qauxv.util.SyncUtils
import io.github.qauxv.util.dexkit.DexKit
import io.github.qauxv.util.dexkit.DexKitTarget
import java.util.Arrays

abstract class BaseHookDispatcher<T : IDynamicHook>(
    targets: Array<DexKitTarget>?
) : IDynamicHook, RuntimeErrorTracer {

    private val mErrors: ArrayList<Throwable> = ArrayList()
    private var mInitialized = false
    private var mInitializeResult = false
    private val mDexDeobfIndexes: Array<DexKitTarget>? = targets

    override val isInitialized: Boolean get() = mInitialized

    override val isInitializationSuccessful: Boolean get() = mInitializeResult

    abstract val decorators: Array<T>

    override fun initialize(): Boolean {
        if (mInitialized) {
            return mInitializeResult
        }
        mInitializeResult = try {
            initOnce()
        } catch (e: Throwable) {
            traceError(e)
            // don't throw exception here, except errors like OutOfMemoryError or StackOverflowError
            if (e is Error && e !is AssertionError && e !is LinkageError) {
                // wtf Throwable
                throw e
            }
            false
        }
        mInitialized = true
        return mInitializeResult
    }

    @Throws(Exception::class)
    protected abstract fun initOnce(): Boolean

    override val runtimeErrors: List<Throwable> = mErrors

    override val targetProcesses = SyncUtils.PROC_MAIN

    override val isTargetProcess by lazy { SyncUtils.isTargetProcess(targetProcesses) }

    override val isAvailable = true

    override val isPreparationRequired: Boolean
        get() {
            mDexDeobfIndexes?.let {
                it.forEach { i ->
                    if (DexKit.isRunDexDeobfuscationRequired(i)) {
                        return true
                    }
                }
            }
            return false
        }

    override fun makePreparationSteps(): Array<Step>? = mDexDeobfIndexes?.map { DexDeobfStep(it) }?.toTypedArray()

    override val isApplicationRestartRequired = false

    override var isEnabled: Boolean
        get() {
            decorators.iterator().forEach { if (it.isEnabled) return true }
            return false
        }
        set(_) {
            // not supported
        }

    override fun traceError(e: Throwable) {
        // check if there is already an error with the same error message and stack trace
        var alreadyLogged = false
        for (error in mErrors) {
            if (error.message == e.message && Arrays.equals(error.stackTrace, e.stackTrace)) {
                alreadyLogged = true
            }
        }
        if (!alreadyLogged) {
            mErrors.add(e)
        }
        Log.e(e)
    }
}
