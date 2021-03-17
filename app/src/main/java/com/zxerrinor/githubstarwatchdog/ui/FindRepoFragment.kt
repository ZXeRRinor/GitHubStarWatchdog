package com.zxerrinor.githubstarwatchdog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.omegar.mvp.MvpAppCompatFragment
import com.omegar.mvp.presenter.InjectPresenter
import com.zxerrinor.githubstarwatchdog.CurrentValuesStore
import com.zxerrinor.githubstarwatchdog.R
import com.zxerrinor.githubstarwatchdog.databinding.FragmentFindRepoBinding
import com.zxerrinor.githubstarwatchdog.presenters.FindRepoPresenter
import com.zxerrinor.githubstarwatchdog.views.FindRepoView

class FindRepoFragment : MvpAppCompatFragment(), FindRepoView {
    private var _binding: FragmentFindRepoBinding? = null
    private val binding get() = _binding!!

    @InjectPresenter
    lateinit var findRepoPresenter: FindRepoPresenter
//    private val presenter by moxyPresenter { presenterProvider.get() }

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
            if(binding.repoInput.selectedItem == null) {
                CurrentValuesStore.activity.runOnUiThread {
                    Toast.makeText(
                        CurrentValuesStore.activity, "Please, select repository for watching",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return@setOnClickListener
            }
            findRepoPresenter.onLoadButtonClicked(
                binding.repoInput.selectedItem.toString()
            )
            findNavController().navigate(R.id.action_FindRepoFragment_to_ShowChartFragment)
        }

        binding.findUserRepos.setOnClickListener {
            findRepoPresenter.onFindRepoButtonClicked(binding.username.text.toString())
        }

        binding.offlineModeSwitch.setOnClickListener {
            findRepoPresenter.onOfflineModeSwitchClicked()
        }

        binding.hideNotLoadedSwitch.setOnClickListener {
            findRepoPresenter.hideNotLoaded = !findRepoPresenter.hideNotLoaded
        }
    }

    override fun setRepoInputAdapter(repoList: List<String>) {
        val repoInputAdapter = ArrayAdapter(
            CurrentValuesStore.activity,
            android.R.layout.simple_spinner_item,
            repoList
        )
        repoInputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        CurrentValuesStore.activity.runOnUiThread {
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