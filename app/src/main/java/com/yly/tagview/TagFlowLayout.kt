package com.yly.tagview

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.Nullable
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import org.jetbrains.annotations.NotNull
import java.util.*
import kotlin.collections.HashSet

/**
 * 标签
 */
class TagFlowLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    FlexboxLayout(context, attrs, defStyle), OnDataChangedListener {

    companion object {
        private val TAG = "TagFlowLayout"

        private val KEY_CHOOSE_POS = "key_choose_pos"
        private val KEY_DEFAULT = "key_default"
    }

    var adapter: TagAdapter<*>? = null
        set(@NotNull adapter) {
            field = adapter
            field!!.setOnDataChangedListener(this@TagFlowLayout)
            refreshAllTags()
        }

    private var atleastOneSelect = true//是否至少选中一个
    private var mSelectedMax: Int = 0//-1为不限制数量

    private val mSelectedView = HashSet<Int>()

    private var mOnTagClickListener: OnTagClickListener? = null

    val selectedList: Set<Int>
        get() = HashSet(mSelectedView)

    interface OnTagClickListener {
        fun onTagClick(view: TagView, position: Int)
    }

    init {
        flexWrap = FlexWrap.WRAP
        val ta = context.obtainStyledAttributes(attrs, R.styleable.TagFlowLayout)
        mSelectedMax = ta.getInt(R.styleable.TagFlowLayout_max_select, -1)
        atleastOneSelect = ta.getBoolean(R.styleable.TagFlowLayout_atleast_one, true)
        ta.recycle()
    }

    fun setOnTagClickListener(@Nullable onTagClickListener: OnTagClickListener) {
        mOnTagClickListener = onTagClickListener
    }

    /**
     * 刷新所有标签
     */
    private fun refreshAllTags() {
        //清空选中
        mSelectedView.clear()
        removeAllViews()
        for (i in 0 until adapter!!.count) {
            addTagView(i)
        }
    }

    private fun addTagView(position: Int) {
        val tagView =
            (adapter as TagAdapter<Any>).getView(
                this,
                position,
                (adapter as TagAdapter<Any>).getItem(position)
            )
        val tagViewContainer = TagView(context)
        tagView.isDuplicateParentStateEnabled = true
        if (tagView.layoutParams != null) {
            tagViewContainer.layoutParams = tagView.layoutParams
        } else {
            val lp = MarginLayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            //边距
            val commonMargin = context.dip(5)
            lp.setMargins(
                commonMargin,
                commonMargin,
                commonMargin,
                commonMargin
            )
            tagViewContainer.layoutParams = lp
            tagView.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }
        tagViewContainer.addView(tagView)
        addView(tagViewContainer)
        if ((adapter as TagAdapter<Any>).setSelected(
                position,
                (adapter as TagAdapter<Any>).getItem(position)
            )
        ) {
            doSelect(tagViewContainer, position, false)
        }
        tagView.isClickable = false
        tagViewContainer.setOnClickListener { v ->
            doSelect(tagViewContainer, position, true)
            mOnTagClickListener?.onTagClick(tagViewContainer, position)
        }
    }

    fun setMaxSelectCount(count: Int) {
        if (mSelectedView.size > count) {
            Log.w(TAG, "you has already select more than $count views , so it will be clear .")
            mSelectedView.clear()
        }
        mSelectedMax = count
    }

    /**
     * 设置选中状态
     */
    private fun setChildChecked(position: Int, view: TagView, fromUser: Boolean) {
        view.isChecked = true
        (adapter as TagAdapter<Any>).onSelected(
            (adapter as TagAdapter<Any>).getItem(position),
            position,
            view,
            fromUser
        )
    }

    /**
     * 设置未选中状态
     */
    private fun setChildUnChecked(position: Int, view: TagView, fromUser: Boolean) {
        view.isChecked = false
        (adapter as TagAdapter<Any>).unSelected(
            (adapter as TagAdapter<Any>).getItem(position),
            position,
            view,
            fromUser
        )
    }

    /**
     * 标签选中
     */
    private fun doSelect(child: TagView, position: Int, fromUser: Boolean) {
        if (!child.isChecked) {
            //处理max_select=1的情况
            if (mSelectedMax == 1 && mSelectedView.size == 1) {
                val iterator = mSelectedView.iterator()
                val preIndex = iterator.next()
                val pre = getChildAt(preIndex) as TagView
                setChildUnChecked(preIndex, pre, fromUser)
                setChildChecked(position, child, fromUser)
                mSelectedView.remove(preIndex)
                mSelectedView.add(position)
            } else {
                if (mSelectedMax > 0 && mSelectedView.size >= mSelectedMax) {
                    //当前已经达到最大选择数量
                    return
                }
                setChildChecked(position, child, fromUser)
                mSelectedView.add(position)
            }
        } else {
            if (mSelectedMax == 1 && atleastOneSelect) {
                //必须选一个
                return
            }
            setChildUnChecked(position, child, fromUser)
            mSelectedView.remove(position)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(KEY_DEFAULT, super.onSaveInstanceState())

        var selectPos = StringBuilder()
        if (mSelectedView.size > 0) {
            for (key in mSelectedView) {
                selectPos.append(key).append("|")
            }
            selectPos = StringBuilder(selectPos.substring(0, selectPos.length - 1))
        }
        bundle.putString(KEY_CHOOSE_POS, selectPos.toString())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val mSelectPos = state.getString(KEY_CHOOSE_POS)
            if (!mSelectPos.isNullOrBlank()) {
                val split = mSelectPos.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (pos in split) {
                    val index = Integer.parseInt(pos)
                    mSelectedView.add(index)
                    val tagView = getChildAt(index) as TagView
                    setChildChecked(index, tagView, false)
                }
            }
            super.onRestoreInstanceState(state.getParcelable(KEY_DEFAULT))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun onChanged() {
        refreshAllTags()
    }

    override fun onDataAdd(position: Int) {
        addTagView(position)
    }

    override fun onDataAddRange(start: Int, end: Int) {
        for (i in start..end) {
            addTagView(i)
        }
    }

    override fun onDataRemove(position: Int) {
        mSelectedView.remove(position)
        removeViewAt(position)
        for (i in position until childCount) {
            val view = getChildAt(i) as TagView
            view.setOnClickListener { _ ->
                doSelect(view, i, true)
                mOnTagClickListener?.onTagClick(view, i)
            }
        }
    }

    override fun onDataRemoveAll() {
        mSelectedView.clear()
        removeAllViews()
    }

    /**
     * 标签数据适配器
     */
    abstract class TagAdapter<T> {

        constructor(datas: MutableList<T>) {
            mTagDatas = datas
        }

        constructor() {
            mTagDatas = ArrayList()
        }

        //数据
        var mTagDatas: MutableList<T> = arrayListOf()

        private var onDataChangedListener: OnDataChangedListener? = null

        val count: Int
            get() = mTagDatas.size

        /**
         * 内部 TagFlowLayout 设置监听
         */
        internal fun setOnDataChangedListener(listener: OnDataChangedListener) {
            onDataChangedListener = listener
        }

        fun notifyDataChanged() {
            onDataChangedListener?.onChanged()
        }

        fun getItem(position: Int): T {
            return mTagDatas.get(position)
        }

        fun addData(data: T) {
            mTagDatas.apply {
                add(data)
                onDataChangedListener?.onDataAdd(mTagDatas.size - 1)
            }
        }

        fun addData(data: List<T>) {
            mTagDatas.apply {
                val start = size
                addAll(data)
                onDataChangedListener?.onDataAddRange(start, size - 1)
            }
        }

        fun remove(position: Int) {
            mTagDatas.apply {
                removeAt(position)
                onDataChangedListener?.onDataRemove(position)
            }
        }

        fun removeAll() {
            mTagDatas.apply {
                clear()
                onDataChangedListener?.onDataRemoveAll()
            }
        }

        abstract fun getView(parent: FlexboxLayout, position: Int, data: T): View

        fun onSelected(data: T, position: Int, view: TagView, fromUser: Boolean) {}

        fun unSelected(data: T, position: Int, view: TagView, fromUser: Boolean) {}

        fun setSelected(position: Int, data: T): Boolean {
            return false
        }
    }
}

internal interface OnDataChangedListener {
    /**
     * 数据完全变化
     */
    fun onChanged()

    /**
     * 数据增加
     */
    fun onDataAdd(position: Int)

    fun onDataAddRange(start: Int, end: Int)

    fun onDataRemove(position: Int)

    fun onDataRemoveAll()
}
