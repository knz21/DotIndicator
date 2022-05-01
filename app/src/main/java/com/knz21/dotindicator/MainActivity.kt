package com.knz21.dotindicator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.knz21.dotindicator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {

        private const val ITEM_COUNT = 20
    }

    private lateinit var binding: ActivityMainBinding

    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.position.text = position.toString()
        binding.minus.setOnClickListener { updatePosition(false) }
        binding.plus.setOnClickListener { updatePosition(true) }
        binding.indicator1.setItemCount(ITEM_COUNT)
        binding.indicator2.setItemCount(ITEM_COUNT)
        binding.indicator3.setItemCount(ITEM_COUNT)
    }

    private fun updatePosition(isPlus: Boolean) {
        position = (position + if (isPlus) 1 else -1).coerceIn(0, ITEM_COUNT - 1)
        binding.position.text = position.toString()
        binding.indicator1.updatePosition(position)
        binding.indicator2.updatePosition(position)
        binding.indicator3.updatePosition(position)
    }
}