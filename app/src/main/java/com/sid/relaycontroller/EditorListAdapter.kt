package com.sid.relaycontroller

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView

class EditorListAdapter(var data: List<EditorListItem>) :
	RecyclerView.Adapter<EditorListAdapter.EditorListItem>() {

	inner class EditorListItem(var v: View) : RecyclerView.ViewHolder(v) {
		val relayNumber: Spinner = v.findViewById(R.id.relay_number)
		var relayState: Spinner = v.findViewById(R.id.relay_state)
		var relayDuration: EditText = v.findViewById(R.id.relay_duration)

		init {
			(relayNumber.adapter as ArrayAdapter<*>).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
			(relayState.adapter as ArrayAdapter<*>).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorListItem {
		val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
		return EditorListItem(v)
	}

	override fun getItemCount(): Int = data.size
	override fun onBindViewHolder(holder: EditorListItem, position: Int) {

		holder.relayNumber.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(
				parent: AdapterView<*>,
				view: View,
				position: Int,
				id: Long
			) = data[position].relayNumber.setSelection(position)

			override fun onNothingSelected(parent: AdapterView<*>) {}
		}

		holder.relayState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(
				parent: AdapterView<*>,
				view: View,
				position: Int,
				id: Long
			) = data[position].relayState.setSelection(position)

			override fun onNothingSelected(parent: AdapterView<*>) {}
		}

		holder.relayDuration.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(s: Editable?) {}
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) =
				data[holder.adapterPosition].relayDuration.setText(s)
		})
	}

	fun addItem(item: EditorListAdapter.EditorListItem) {
		data += item
		notifyItemInserted(data.size - 1)
	}

	fun removeItem(position: Int) {
		data -= data[position]
		notifyItemRemoved(position)
	}

	fun restoreItem(item: EditorListAdapter.EditorListItem, position: Int) {
		data += item
		notifyItemInserted(position)
	}
}
