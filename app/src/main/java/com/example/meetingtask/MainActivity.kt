package com.example.meetingtask

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meetingtask.databinding.ActivityMainBinding
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
