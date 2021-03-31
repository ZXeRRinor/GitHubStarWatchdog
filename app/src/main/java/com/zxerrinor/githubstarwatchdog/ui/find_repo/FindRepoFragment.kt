package com.zxerrinor.githubstarwatchdog.ui.find_repo

import android.os.Bundle
import android.widget.*
import androidx.core.view.isVisible
import com.google.android.material.switchmaterial.SwitchMaterial
import com.omega_r.adapters.OmegaSpinnerAdapter
import com.omega_r.base.components.OmegaFragment
import com.omegar.mvp.ktx.providePresenter
import com.zxerrinor.githubstarwatchdog.R
import com.zxerrinor.githubstarwatchdog.ui.base.BaseFragment

class FindRepoFragment : BaseFragment(R.layout.fragment_find_repo), FindRepoView {

    override val presenter: FindRepoPresenter by providePresenter()

    private val repoSpinner: Spinner by bind(R.id.spinner_repo) {
        adapter = repoInputAdapter
    }
    private val userNameEditText: EditText by bind(R.id.input_username)
    private val hideNotLoadedSwitch: SwitchMaterial by bind(R.id.switch_hide_not_loaded)
    private val repoInputAdapter: OmegaSpinnerAdapter.StringAdapter by bind(init = {
        return@bind OmegaSpinnerAdapter.StringAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setClickListener(R.id.button_load) {
            presenter.onLoadButtonClicked(repoSpinner.selectedItem?.toString())
        }
        setClickListener(R.id.switch_offline_mode, presenter::onOfflineModeSwitchClicked)
        setClickListener(R.id.button_find_user_repos) {
            presenter.onFindRepoButtonClicked(userNameEditText.text.toString())
        }
    }

    override fun setRepoSpinnerContent(repoList: List<String>) {
        activity?.runOnUiThread { repoInputAdapter.list = repoList }
    }

    override fun setHideNotLoadedSwitchVisibility(visible: Boolean) {
        hideNotLoadedSwitch.isVisible = visible
    }

}