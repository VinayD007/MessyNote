package com.oneline.notes

import android.os.Build
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

/**
 * Custom TextToolbar implementation that exposes the app's "Mono" formatting action
 * directly inside Android's native floating ActionMode toolbar alongside standard
 * Copy, Cut, Paste, Select All, and system Process Text actions (e.g. Read aloud, Translate).
 */
class MonoTextToolbar(
    private val view: View,
    private val onMonoRequested: () -> Unit,
    private val canApplyMono: () -> Boolean
) : TextToolbar {

    private var actionMode: ActionMode? = null
    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
        private set

    private var currentRect: Rect = Rect.Zero
    private var onCopy: (() -> Unit)? = null
    private var onPaste: (() -> Unit)? = null
    private var onCut: (() -> Unit)? = null
    private var onSelectAll: (() -> Unit)? = null
    private var onAutofill: (() -> Unit)? = null

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        showMenu(
            rect = rect,
            onCopyRequested = onCopyRequested,
            onPasteRequested = onPasteRequested,
            onCutRequested = onCutRequested,
            onSelectAllRequested = onSelectAllRequested,
            onAutofillRequested = null
        )
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
        onAutofillRequested: (() -> Unit)?
    ) {
        currentRect = rect
        onCopy = onCopyRequested
        onPaste = onPasteRequested
        onCut = onCutRequested
        onSelectAll = onSelectAllRequested
        onAutofill = onAutofillRequested

        val isSelectionActive = onCopy != null || onCut != null || canApplyMono()

        if (actionMode == null) {
            status = TextToolbarStatus.Shown

            val callback = object : ActionMode.Callback2() {
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    if (mode == null || menu == null) return false

                    // Standard text editing items
                    if (onCopy != null) {
                        menu.add(Menu.NONE, MENU_COPY, 0, android.R.string.copy)
                            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    }
                    if (onCut != null) {
                        menu.add(Menu.NONE, MENU_CUT, 1, android.R.string.cut)
                            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    }
                    if (onPaste != null) {
                        menu.add(Menu.NONE, MENU_PASTE, 2, android.R.string.paste)
                            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    }
                    if (onSelectAll != null) {
                        menu.add(Menu.NONE, MENU_SELECT_ALL, 3, android.R.string.selectAll)
                            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                    }

                    // App-provided "Mono" action when text selection is active
                    if (isSelectionActive) {
                        menu.add(Menu.NONE, MENU_MONO, 4, "Mono")
                            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    }

                    if (onAutofill != null && Build.VERSION.SDK_INT >= 27) {
                        menu.add(Menu.NONE, MENU_AUTOFILL, 5, android.R.string.autofill)
                            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                    }

                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    if (menu != null) {
                        val monoItem = menu.findItem(MENU_MONO)
                        val shouldShow = onCopy != null || onCut != null || canApplyMono()
                        if (shouldShow && monoItem == null) {
                            menu.add(Menu.NONE, MENU_MONO, 4, "Mono")
                                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                        } else if (!shouldShow && monoItem != null) {
                            menu.removeItem(MENU_MONO)
                        }
                    }
                    return true
                }

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    when (item?.itemId) {
                        MENU_COPY -> {
                            onCopy?.invoke()
                            mode?.finish()
                            return true
                        }
                        MENU_CUT -> {
                            onCut?.invoke()
                            mode?.finish()
                            return true
                        }
                        MENU_PASTE -> {
                            onPaste?.invoke()
                            mode?.finish()
                            return true
                        }
                        MENU_SELECT_ALL -> {
                            onSelectAll?.invoke()
                            mode?.invalidate()
                            return true
                        }
                        MENU_AUTOFILL -> {
                            onAutofill?.invoke()
                            mode?.finish()
                            return true
                        }
                        MENU_MONO -> {
                            onMonoRequested()
                            mode?.finish()
                            return true
                        }
                        else -> return false
                    }
                }

                override fun onDestroyActionMode(mode: ActionMode?) {
                    actionMode = null
                    status = TextToolbarStatus.Hidden
                }

                override fun onGetContentRect(mode: ActionMode?, view: View?, outRect: android.graphics.Rect?) {
                    outRect?.set(
                        currentRect.left.toInt(),
                        currentRect.top.toInt(),
                        currentRect.right.toInt(),
                        currentRect.bottom.toInt()
                    )
                }
            }

            actionMode = view.startActionMode(callback, ActionMode.TYPE_FLOATING)
        } else {
            actionMode?.invalidate()
        }
    }

    override fun hide() {
        status = TextToolbarStatus.Hidden
        actionMode?.finish()
        actionMode = null
    }

    companion object {
        const val MENU_COPY = 1001
        const val MENU_CUT = 1002
        const val MENU_PASTE = 1003
        const val MENU_SELECT_ALL = 1004
        const val MENU_MONO = 1005
        const val MENU_AUTOFILL = 1006
    }
}
