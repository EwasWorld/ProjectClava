package com.eywa.projectclava.main.features.ui.confirmDialog

import com.eywa.projectclava.main.features.ui.editNameDialog.NamedItem

/**
 * This state is different to the other dialog states (which are interfaces) because there could be multiple
 * confirm dialogs on a single screen
 */
data class ConfirmDialogState<T : NamedItem>(
        val item: T,
)

//sealed interface ConfirmDialogState<A, T: NamedItem> {
//        val item: T
//
//        data class Standard<T: NamedItem>(override val item: T) : ConfirmDialogState<Any, T>
//        interface Indirect<A, T: NamedItem> : ConfirmDialogState<Any, T> {
//                val storedItem: A
//
//                override val item: T
//                        get() = getItem(storedItem)
//
//                fun getItem(item: A): T
//        }
//}