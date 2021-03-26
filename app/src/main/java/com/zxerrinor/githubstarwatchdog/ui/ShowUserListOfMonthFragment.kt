package com.zxerrinor.githubstarwatchdog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.zxerrinor.githubstarwatchdog.MONTH_ARGUMENT_NAME
import com.zxerrinor.githubstarwatchdog.MONTH_NUMBERS_ARGUMENT_NAME
import com.zxerrinor.githubstarwatchdog.adapters.OnUserItemClickListener
import com.zxerrinor.githubstarwatchdog.adapters.UsersAdapter
import com.zxerrinor.githubstarwatchdog.databinding.FragmentShowUserListOfMonthBinding

class ShowUserListOfMonthFragment : Fragment(), OnUserItemClickListener {
    private var _binding: FragmentShowUserListOfMonthBinding? = null
    private val binding get() = _binding!!
    private var months = mapOf<Byte, List<String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowUserListOfMonthBinding.inflate(inflater, container, false)
        val monthNumbers = arguments?.getByteArray(MONTH_NUMBERS_ARGUMENT_NAME)
            ?: throw IllegalArgumentException("$MONTH_NUMBERS_ARGUMENT_NAME not found in arguments")
        months = monthNumbers.map {
            it to (arguments?.getStringArrayList(it.toString())
                ?: throw IllegalArgumentException("List of users for month \"$it\" not found in arguments"))
        }.toMap()
        val usersAdapter = UsersAdapter(
            months,
            this
        )
        binding.usersOfMonthRV.adapter = usersAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var pos = 0
        var sizeOfList = 0
        val month = arguments?.getByte(MONTH_ARGUMENT_NAME)
            ?: throw IllegalArgumentException("$MONTH_ARGUMENT_NAME not found in arguments")
        months.forEach {
            sizeOfList += it.value.size
            if (it.key - 1 < month) pos += it.value.size
        }
        binding.usersOfMonthRV.layoutManager!!.scrollToPosition(sizeOfList - pos)
    }

    override fun onUserListItemClick(item: String, position: Int) {
        // nothing
    }
}