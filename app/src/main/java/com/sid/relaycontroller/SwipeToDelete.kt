package com.sid.relaycontroller

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

abstract class SwipeToDeleteCallback(context: Context) : ItemTouchHelper.Callback() {
	private val mClearPaint: Paint = Paint()
	private val mBackground: ColorDrawable = ColorDrawable()
	private val backgroundColor: Int = Color.parseColor("#b80f0a")
	private val deleteDrawable: Drawable?

	private val intrinsicWidth: Int
	private val intrinsicHeight: Int

	override fun getMovementFlags(
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder
	): Int = makeMovementFlags(0, ItemTouchHelper.LEFT)

	override fun onMove(
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder,
		viewHolder1: RecyclerView.ViewHolder
	): Boolean = false

	override fun onChildDraw(
		c: Canvas,
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder,
		dX: Float,
		dY: Float,
		actionState: Int,
		isCurrentlyActive: Boolean
	) {
		super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

		val itemView: View = viewHolder.itemView
		val itemHeight: Int = itemView.height

		if (dX == 0f && !isCurrentlyActive) {
			clearCanvas(
				c,
				itemView.right + dX,
				itemView.top.toFloat(),
				itemView.right.toFloat(),
				itemView.bottom.toFloat()
			)

			super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
			return
		}

		mBackground.color = backgroundColor
		mBackground.setBounds(
			itemView.right + dX.toInt(),
			itemView.top,
			itemView.right,
			itemView.bottom
		)
		mBackground.draw(c)

		val deleteIconMargin = (itemHeight - intrinsicHeight) / 2

		val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
		val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
		val deleteIconRight = itemView.right - deleteIconMargin
		val deleteIconBottom = deleteIconTop + intrinsicHeight

		deleteDrawable!!.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
		deleteDrawable.draw(c)

		super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
	}

	private fun clearCanvas(c: Canvas, left: Float, top: Float, right: Float, bottom: Float) =
		c.drawRect(left, top, right, bottom, mClearPaint)

	override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.75f

	init {
		mClearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
		deleteDrawable = ContextCompat.getDrawable(context, R.drawable.ic_delete)

		intrinsicWidth = deleteDrawable!!.intrinsicWidth
		intrinsicHeight = deleteDrawable.intrinsicHeight
	}
}