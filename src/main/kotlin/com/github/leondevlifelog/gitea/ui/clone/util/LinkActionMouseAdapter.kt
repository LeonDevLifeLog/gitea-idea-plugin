/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui.clone.util

import com.intellij.ui.ListUtil
import com.intellij.ui.SimpleColoredComponent
import com.intellij.util.ui.UIUtil
import java.awt.Cursor
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JList

class LinkActionMouseAdapter(private val list: JList<*>) : MouseAdapter() {
    private fun getActionAt(e: MouseEvent): ActionListener? {
        val point = e.point
        val renderer = ListUtil.getDeepestRendererChildComponentAt(list, point)
        if (renderer !is SimpleColoredComponent) return null
        val tag = renderer.getFragmentTagAt(point.x)
        return if (tag is ActionListener) tag else null
    }

    override fun mouseMoved(e: MouseEvent) {
        val action = getActionAt(e)
        if (action != null) {
            UIUtil.setCursor(list, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
        }
        else {
            UIUtil.setCursor(list, Cursor.getDefaultCursor())
        }
    }

    override fun mouseClicked(e: MouseEvent) {
        getActionAt(e)?.actionPerformed(ActionEvent(e.source, e.id, "execute", e.modifiersEx))
    }
}