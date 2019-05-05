package com.yly.tagview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private val tagAdapter by lazy {
        object : TagFlowLayout.TagAdapter<String>() {
            override fun getView(parent: FlexboxLayout, position: Int, data: String): View {
                return TextView(this@MainActivity).apply {
                    text = data
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val datas = arrayListOf("1", "2", "3")
        tagAdapter.mTagDatas = datas
        tagTest.adapter = tagAdapter
    }
}
