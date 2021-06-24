package org.thoughtcrime.securesms.conversation.v2.dialogs

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_join_open_group.view.*
import network.loki.messenger.R
import org.session.libsession.messaging.contacts.Contact
import org.session.libsession.messaging.open_groups.OpenGroupV2
import org.thoughtcrime.securesms.conversation.v2.utilities.BaseDialog
import org.thoughtcrime.securesms.database.DatabaseFactory

class JoinOpenGroupDialog(private val openGroup: OpenGroupV2) : BaseDialog() {

    override fun setContentView(builder: AlertDialog.Builder) {
        val contentView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_join_open_group, null)
        val name = openGroup.name
        val title = resources.getString(R.string.dialog_join_open_group_title, name)
        contentView.joinOpenGroupTitleTextView.text = title
        val explanation = resources.getString(R.string.dialog_join_open_group_explanation, name)
        val spannable = SpannableStringBuilder(explanation)
        val startIndex = explanation.indexOf(name)
        spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, startIndex + name.count(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        contentView.joinOpenGroupExplanationTextView.text = spannable
        contentView.cancelButton.setOnClickListener { dismiss() }
        contentView.joinButton.setOnClickListener { join() }
        builder.setView(contentView)
    }

    private fun join() {

    }
}