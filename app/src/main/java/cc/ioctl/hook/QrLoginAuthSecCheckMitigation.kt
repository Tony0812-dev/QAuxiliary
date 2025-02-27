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

package cc.ioctl.hook

import android.view.View
import cc.ioctl.util.hookBeforeIfEnabled
import io.github.qauxv.base.annotation.FunctionHookEntry
import io.github.qauxv.base.annotation.UiItemAgentEntry
import io.github.qauxv.dsl.FunctionEntryRouter
import io.github.qauxv.hook.CommonSwitchFunctionHook
import io.github.qauxv.util.Initiator
import io.github.qauxv.util.hostInfo
import java.lang.reflect.Modifier

@FunctionHookEntry
@UiItemAgentEntry
object QrLoginAuthSecCheckMitigation : CommonSwitchFunctionHook() {

    override val name: String = "跳过扫码风险登录五秒确认"

    override val uiItemLocation: Array<String> = FunctionEntryRouter.Locations.Auxiliary.EXPERIMENTAL_CATEGORY

    override fun initOnce(): Boolean {
        val kQRLoginAuthActivity = Initiator.loadClass("com.tencent.biz.qrcode.activity.QRLoginAuthActivity")
        val confirm_risk_login_btn = Initiator.loadClass(hostInfo.packageName + ".R\$id")
            .getDeclaredField("confirm_risk_login_btn").get(null) as Int
        val onClick = kQRLoginAuthActivity.declaredMethods.single {
            it.name == "onClick" && it.parameterTypes.size == 1
        }
        val doLogin = kQRLoginAuthActivity.declaredMethods.single {
            val argt = it.parameterTypes
            argt.size == 1 && argt[0] == java.lang.Boolean.TYPE &&
                it.returnType == Void.TYPE && it.modifiers == Modifier.PROTECTED
        }.also {
            it.isAccessible = true
        }
        hookBeforeIfEnabled(onClick) { params ->
            val this0 = params.thisObject
            val view = params.args[0] as View
            if (view.id == confirm_risk_login_btn) {
                doLogin.invoke(this0, false)
                params.result = null
            }
        }
        return true
    }

}
