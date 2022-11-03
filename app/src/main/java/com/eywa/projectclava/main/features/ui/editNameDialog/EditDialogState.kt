package com.eywa.projectclava.main.features.ui.editNameDialog

interface EditDialogState<T : NamedItem> {
    val editItemName: String
    val editNameIsDirty: Boolean
    val editDialogOpenFor: T?

    fun editItemCopy(
            editItemName: String = this.editItemName,
            editNameIsDirty: Boolean = this.editNameIsDirty,
            editDialogOpenFor: T? = this.editDialogOpenFor,
    ): EditDialogState<T>
}