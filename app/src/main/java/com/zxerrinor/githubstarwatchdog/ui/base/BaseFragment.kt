package com.zxerrinor.githubstarwatchdog.ui.base

import androidx.annotation.LayoutRes
import com.omega_r.base.components.OmegaFragment

abstract class BaseFragment : OmegaFragment, BaseView {
    constructor() : super()

    constructor(@LayoutRes id: Int) : super(id)

    abstract override val presenter: BasePresenter<*>
}
