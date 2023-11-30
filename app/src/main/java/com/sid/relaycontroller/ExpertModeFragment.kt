package com.sid.relaycontroller

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class ExpertModeFragment : Fragment() {
	private var listItems: List<EditorListAdapter.EditorListItem> = listOf()
	lateinit var adapter: EditorListAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		super.onCreateView(inflater, container, savedInstanceState)

		val v = inflater.inflate(R.layout.fragment_expert_mode, container, false)
		adapter = EditorListAdapter(ArrayList())

		val recyclerView = v.findViewById<RecyclerView>(R.id.editor_list)
		val handler = Handler(Looper.getMainLooper())

		recyclerView.layoutManager = LinearLayoutManager(activity)
		recyclerView.adapter = adapter

		ItemTouchHelper(object : SwipeToDeleteCallback(requireContext()) {
			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
				val position = viewHolder.adapterPosition
				adapter.removeItem(position)

				Snackbar.make(
					viewHolder.itemView, "Item was removed from the list.", Snackbar.LENGTH_LONG
				).setAction("Undo") {
					adapter.restoreItem(adapter.data[position], position)
					recyclerView.scrollToPosition(position)
				}.setActionTextColor(Color.YELLOW).show()
			}
		}).attachToRecyclerView(recyclerView)

		v.findViewById<View>(R.id.button_add).setOnClickListener {
			adapter.addItem(
				adapter.EditorListItem(
					LayoutInflater.from(requireContext())
						.inflate(R.layout.list_item, recyclerView, false)
				)
			)
		}

		v.findViewById<MaterialButton>(R.id.button_run).setOnClickListener {
			if (adapter.data.isEmpty()) {
				Snackbar.make(
					v,
					"Please add at least one block to the list.",
					Snackbar.LENGTH_LONG
				).show()

				Log.d("ExpertModeFragment", "No items in list")
				return@setOnClickListener
			}

			for (i in 0 until adapter.data.size) {
				v.findViewById<View>(R.id.button_add).isEnabled = false
				it.isEnabled = false

				adapter.data[i].v.findViewById<FrameLayout>(R.id.list_item_frame)
					.setBackgroundResource(R.drawable.list_item_background_current)
				adapter.data[i].v.findViewById<FrameLayout>(R.id.list_item_frame).isEnabled = false
				listItems = adapter.data.toList()

				val item = adapter.data[i]

				if (item.relayDuration.text.isEmpty()) {
					Snackbar.make(
						v,
						"Please set a duration for the relay at block ${i + 1}.",
						Snackbar.LENGTH_LONG
					).show()

					Log.d("ExpertModeFragment", "No duration set at position ${i + 1}")
					v.findViewById<View>(R.id.button_add).isEnabled = true
					it.isEnabled = true
					recyclerView.isEnabled = true

					adapter.data[i].v.findViewById<FrameLayout>(R.id.list_item_frame).isEnabled =
						true
					adapter.data[i].v.findViewById<FrameLayout>(R.id.list_item_frame)
						.setBackgroundResource(R.drawable.list_item_background)

					return@setOnClickListener
				}

				val request =
					MainActivity.HttpRequest("${MainActivity.url}/relay${item.relayNumber.selectedItem}/${item.relayState.selectedItem}/")

				handler.postDelayed(
					{
						Log.e(
							"ExpertModeFragment",
							"${MainActivity.url}/relay${item.relayNumber.selectedItem}/${item.relayState.selectedItem}/"
						)
						request.execute()
					},
					item.relayDuration.text.toString().toLong() * 1000
				)

				adapter.data[i].v.setBackgroundResource(R.drawable.list_item_background)
			}

			v.findViewById<View>(R.id.button_add).isEnabled = true
			it.isEnabled = true
			recyclerView.isEnabled = true

			adapter.data.forEach { item ->
				item.v.findViewById<FrameLayout>(R.id.list_item_frame).isEnabled = true
				item.v.findViewById<FrameLayout>(R.id.list_item_frame)
					.setBackgroundResource(R.drawable.list_item_background)
			}
		}

		return v
	}

	override fun onResume() {
		super.onResume()

		adapter.data = listItems
		adapter.notifyItemRangeChanged(0, adapter.data.size)
	}
}