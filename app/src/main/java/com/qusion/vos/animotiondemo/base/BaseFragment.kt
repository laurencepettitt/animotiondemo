package com.qusion.vos.animotiondemo.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseFragment<DataBindingType : ViewDataBinding>(
    @LayoutRes private val layoutId: Int
) : Fragment() {

    /** The ViewDataBinding. Its initialized in onCreateView().
     * Use it to access the generated view classes.
     * Pass the data variables in the active fragment's onCreateView()*/
    protected lateinit var bind: DataBindingType

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = DataBindingUtil.inflate(
            inflater,
            layoutId,
            container,
            false
        )

        onBind()

        return bind.root
    }

    /** Apply and pass all the argument to the DataBinding of the fragment */
    abstract fun onBind()
}
