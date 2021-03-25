package com.zxerrinor.githubstarwatchdog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.omega_r.base.components.OmegaFragment
import com.omegar.mvp.presenter.InjectPresenter
import com.zxerrinor.githubstarwatchdog.databinding.FragmentFindRepoBinding
import com.zxerrinor.githubstarwatchdog.presenters.FindRepoPresenter
import com.zxerrinor.githubstarwatchdog.views.FindRepoView

class FindRepoFragment : OmegaFragment(), FindRepoView {
    private var _binding: FragmentFindRepoBinding? = null
    private val binding get() = _binding!!

    @InjectPresenter
    override lateinit var presenter: FindRepoPresenter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindRepoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLoad.setOnClickListener {
            if (binding.repoInput.selectedItem == null) {
                val activity =
                    activity ?: throw IllegalStateException("FindRepoFragment must be in activity")
                activity.runOnUiThread {
                    Toast.makeText(
                        activity, "Please, select repository for watching",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return@setOnClickListener
            }
            presenter.onLoadButtonClicked(
                binding.repoInput.selectedItem.toString()
            )
        }

        binding.buttonFindUserRepos.setOnClickListener {
            presenter.onFindRepoButtonClicked(binding.inputUsername.text.toString())
        }

        binding.offlineModeSwitch.setOnClickListener {
            presenter.onOfflineModeSwitchClicked()
        }

        binding.hideNotLoadedSwitch.setOnClickListener {
            presenter.hideNotLoaded = !presenter.hideNotLoaded
        }
    }

    override fun setRepoInputAdapter(repoList: List<String>) {
        val activity = activity ?: throw IllegalStateException("Fragment must be in activity")
        val repoInputAdapter = ArrayAdapter(
            activity,
            android.R.layout.simple_spinner_item,
            repoList
        )
        repoInputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        activity.runOnUiThread {
            binding.repoInput.adapter = repoInputAdapter
            repoInputAdapter.notifyDataSetChanged()
        }
    }

    override fun setHideNotLoadedSwitchVisibility(visible: Boolean) {
        binding.hideNotLoadedSwitch.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}