package com.mitkoned.prayerneeds

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mitkoned.prayerneeds.adapters.PrayerNeedAdapter
import com.mitkoned.prayerneeds.entities.PrayerNeed

abstract class PrayerNeedSwipeGestures(
        //context: Context,
        dragDirs: Int,
        swipeDirs: Int
) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    lateinit var removedPrayerNeed: PrayerNeed

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        //viewHolder.itemView.parent
        //get recyclerView from holder?

        when(direction) {
            ItemTouchHelper.LEFT -> {
                //Delete item or archive it
                /*
                * removed = list[position]
                * list.removeAt(position)
                * adapter.notifyItemRemoved(position)
                *
                * Snackbar.make(recyclerView, removed, Snackbar.LENGTH_LONG)
                *         .setAction("Undo") { // on click listener
                *             list.add(position, removed)
                *             adapter.notifyItemInserted(position)
                *         }.show()
                * */
                //onSwiped(viewHolder, ItemTouchHelper.RIGHT)
            }
            ItemTouchHelper.RIGHT -> {
                (viewHolder.itemView.parent as RecyclerView).adapter?.notifyItemChanged(position)

//              viewHolder.itemView.findViewById<ConstraintLayout>(R.id.cl_prayerNeedForeground)
//                      .animate()
//                      .withEndAction {}.translationX(0f)
            }
        }
    }

    /*
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        //c.clipBounds.set()
//        c.setMatrix(Matrix(c.matrix).apply {
//            val percent = dX / 704f
//            setScale(percent, 1f)
//        })
        //c.clipRect(viewHolder.itemView.clipBounds)
        //c.drawColor(Color.GREEN)

        val view = viewHolder.itemView
        c.apply {
            drawText("Edit", 0f, view.bottom.toFloat(), Paint().apply {
                color = Color.WHITE
                translate(0f, -view.height.toFloat() / 2f)
                textSize = 48f
            })

            translate(0f, view.height.toFloat() / 2f)

            translate(dX, 0f)
            drawRect(Rect().also {
                it.top = view.top
                it.bottom = view.bottom
                it.left = view.right
                it.right = view.right + view.width
            }, Paint().apply {
                this.color = Color.parseColor("#ff8000")
            })
//            drawRect(Rect().also {
//                it.top = view.top
//                it.bottom = view.bottom
//                it.left = view.left - view.width
//                it.right = view.left
//            }, Paint().apply {
//                this.color = Color.argb(dX / (view.width * 0.7f), 0.5f, 0f, 1f )
//            })
            drawRect(Rect().also {
                it.top = view.top
                it.bottom = view.bottom
                it.left = view.left - view.width
                it.right = view.left
            }, Paint().apply {
                this.color = Color.argb(Math.min(dX / (view.width * 1.5f), 0.5f), 0f, 1f, 1f )
                this.blendMode = BlendMode.SCREEN
            })



//            view.context.applicationContext.getDrawable(R.drawable.ic_edit_prayer_need)?.let {
//                val c1 = Canvas()
//                it.draw(c1)
//                this.drawBitmap(Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888, true),
//                        view.left.toFloat(), view.top.toFloat(), Paint().apply { color = Color.BLACK })
//            }
        }



        viewHolder.itemView.findViewById<TextView>(R.id.tvDesc).text = "$dX $dY"
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
    // */

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder != null) {
            val foregroundView = (viewHolder as PrayerNeedAdapter.PrayerNeedViewHolder).foregroundView
            ItemTouchHelper.Callback.getDefaultUIUtil().onSelected(foregroundView)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val foregroundView = (viewHolder as PrayerNeedAdapter.PrayerNeedViewHolder).foregroundView
        ItemTouchHelper.Callback.getDefaultUIUtil().clearView(foregroundView)
    }

    var showedAction = false
    var prevdX = 0f
    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean)
    {
        val foregroundView = (viewHolder as PrayerNeedAdapter.PrayerNeedViewHolder).foregroundView
        val backgroundView = viewHolder.itemView.findViewById<ConstraintLayout>(R.id.cl_prayerNeedBackground)
        
        if (prevdX >= 0 && dX <= 0 ||
            prevdX <= 0 && dX >= 0) {
            showedAction = false
        }
        
        if (dX < 0) {
            backgroundView.background = ColorDrawable(Color.parseColor("#ff2000"))
            if (!showedAction) {
                backgroundView.findViewById<View>(R.id.cl_prayerNeedBackgroundLeft).visibility = View.INVISIBLE
                backgroundView.findViewById<View>(R.id.cl_prayerNeedBackgroundRight).visibility = View.VISIBLE
                showedAction = true
            }
        } else {
            backgroundView.background = ColorDrawable(Color.parseColor("#8000ff"))
            if (!showedAction) {
                backgroundView.findViewById<View>(R.id.cl_prayerNeedBackgroundLeft).visibility = View.VISIBLE
                backgroundView.findViewById<View>(R.id.cl_prayerNeedBackgroundRight).visibility = View.INVISIBLE
                showedAction = true
            }
            
        }

        prevdX = dX
        ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean)
    {
        val foregroundView = (viewHolder as PrayerNeedAdapter.PrayerNeedViewHolder).foregroundView
        c.translate(dX, 0f)
        c.drawRect(Rect().also {
            it.top = viewHolder.itemView.top
            it.bottom = viewHolder.itemView.bottom
            it.left = 0
            it.right = foregroundView.width
        }, Paint().apply {
            if (dX < 0) {
                this.color = Color.argb(-dX / (foregroundView.width * 1.1f), 1f, 0.125f, 0f)
            } else {
                this.color = Color.argb(dX / (foregroundView.width * 1.1f), 0.5f, 0f, 1f)
            }
        })

        ItemTouchHelper.Callback.getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
    }
}