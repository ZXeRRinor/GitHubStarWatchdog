package com.zxerrinor.githubstarwatchdog.ui.show_user_list_of_month

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.omega_r.libs.omegarecyclerview.OmegaRecyclerView
import com.omegar.mvp.ktx.providePresenter
import com.zxerrinor.githubstarwatchdog.MONTH_ARGUMENT_NAME
import com.zxerrinor.githubstarwatchdog.MONTH_NUMBERS_ARGUMENT_NAME
import com.zxerrinor.githubstarwatchdog.R
import com.zxerrinor.githubstarwatchdog.ui.base.BaseFragment

class ShowUserListOfMonthFragment : BaseFragment(R.layout.fragment_show_user_list_of_month), ShowUserListOfMonthView {

    override val presenter: ShowUserListOfMonthPresenter by providePresenter()

    private val usersOfMonthRv: OmegaRecyclerView by bind(R.id.rv_users_of_month)

    private var months = mapOf<Byte, ArrayList<String>>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        months = (arguments?.getByteArray(MONTH_NUMBERS_ARGUMENT_NAME)
            ?: throw IllegalArgumentException("$MONTH_NUMBERS_ARGUMENT_NAME not found in arguments")).map {
            it to (arguments?.getStringArrayList(it.toString())
                ?: throw IllegalArgumentException("List of users for month \"$it\" not found in arguments"))
        }.toMap()
        usersOfMonthRv.adapter = UsersAdapter(months)

        var pos = 0
        var sizeOfList = 0
        val month = arguments?.getByte(MONTH_ARGUMENT_NAME)
            ?: throw IllegalArgumentException("$MONTH_ARGUMENT_NAME not found in arguments")
        months.forEach {
            sizeOfList += it.value.size
            if (it.key - 1 < month) pos += it.value.size
        }
        usersOfMonthRv.layoutManager!!.scrollToPosition(sizeOfList - pos)
    }
}