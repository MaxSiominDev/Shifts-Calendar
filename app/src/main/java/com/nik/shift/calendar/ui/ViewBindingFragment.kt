package com.nik.shift.calendar.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class ViewBindingFragment<out VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = createViewBindingInstance(this, inflater, container)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private fun <VB : ViewBinding> createViewBindingInstance(
            classInstance: Any, inflater: LayoutInflater, container: ViewGroup?
        ): VB {
            var genericViewBindingClassType: Type? = null

            // Finds in the class' hierarchy a class that has generics in its type arguments
            var klass: Class<*>? = classInstance::class.java
            search@ while (klass != null) {
                val type = klass.genericSuperclass
                klass = if (type is ParameterizedType) {
                    // Returns the 1st available ViewBinding
                    // on the class' generic argument list, or null if nothing found
                    genericViewBindingClassType = type.actualTypeArguments.find {
                        ViewBinding::class.java.isAssignableFrom(it as Class<*>)
                    }

                    if (genericViewBindingClassType != null) {
                        break@search
                    }

                    // Has generics but there is no ViewBinding among them, continue searching
                    type.rawType as Class<*>
                } else {
                    // Has no generics, continue searching
                    klass.superclass
                }
            }

            @Suppress("UNCHECKED_CAST")
            val actualViewBindingClassType = genericViewBindingClassType as? Class<VB>
                ?: throw InstantiationException("ViewBinding not found in generic arguments of the class")

            val inflate = actualViewBindingClassType.getMethod(
                "inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
            )

            @Suppress("UNCHECKED_CAST")
            return inflate.invoke(null, inflater, container, false) as VB
        }

    }
}
