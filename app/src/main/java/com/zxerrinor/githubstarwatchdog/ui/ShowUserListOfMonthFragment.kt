package com.zxerrinor.githubstarwatchdog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.zxerrinor.githubstarwatchdog.CurrentValuesStore
import com.zxerrinor.githubstarwatchdog.adapters.OnUserItemClickListener
import com.zxerrinor.githubstarwatchdog.adapters.UsersAdapter
import com.zxerrinor.githubstarwatchdog.databinding.FragmentShowUserListOfMonthBinding

class ShowUserListOfMonthFragment : Fragment(), OnUserItemClickListener {
    private var _binding: FragmentShowUserListOfMonthBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowUserListOfMonthBinding.inflate(inflater, container, false)
        val usersAdapter = UsersAdapter(
            CurrentValuesStore.months,
            this
        )
        binding.usersOfMonthRV.adapter = usersAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.usersOfMonthRV.layoutManager = LinearLayoutManager(CurrentValuesStore.activity)
        var pos = 0
        CurrentValuesStore.months.forEach {
            if(it.key < CurrentValuesStore.month) pos += it.value.size
        }
        binding.usersOfMonthRV.layoutManager!!.scrollToPosition(pos)
    }

    override fun onUserListItemClick(item: String, position: Int) {
        // nothing
    }
}