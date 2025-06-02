package com.example.crudnews

import android.app.Activity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.crudnews.ui.theme.CrudNewsTheme

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.jvm.java
import kotlin.text.clear
import kotlin.text.get


class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var adapter: NewsAdapter
    private var newsList = mutableListOf<News>()
    private val db = Firebase.firestore
    private lateinit var rcv :RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rcv = findViewById<RecyclerView>(R.id.rcvNews)

        recyclerView = findViewById(R.id.rcvNews)
        fab = findViewById(R.id.floatAddNews)
        fab.setOnClickListener {
            startActivity(Intent(this, AddEditNewsActivity::class.java))
        }
        fetchNews()
    }

    private fun fetchNews() {
        db.collection("news").get().addOnSuccessListener { result ->
            newsList.clear()
            for(document in result){
                val news = document.toObject(News::class.java)
                news.id = document.id
                newsList.add(news)
            }

            adapter = NewsAdapter(newsList,
                onItemClick = { selectedNews ->
                    val options = arrayOf("Lihat", "Edit")
                    val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                    builder.setTitle("Pilih Aksi")
                    builder.setItems(options){ _, which ->
                        when(which){
                            0 -> {
                                val intent = Intent(this, DetailNewsActivity::class.java)
                                intent.putExtra("news", selectedNews)
                                startActivity(intent)
                            }
                            1 -> {
                                val intent = Intent(this, AddEditNewsActivity::class.java)
                                intent.putExtra("news", selectedNews)
                                startActivity(intent)
                            }
                        }
                    }
                    builder.show()
            }, onDeleteClick = {
                selectedNews -> selectedNews.id?.let{
                    docId ->
                    db.collection("news").document(docId).delete().addOnSuccessListener {
                        Toast.makeText(this,"Berhasil dihapus", Toast.LENGTH_SHORT).show()
                        fetchNews()
                    }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal menghapus", Toast.LENGTH_SHORT).show()
                        }
                }
            })
            rcv.layoutManager = LinearLayoutManager(this)
            rcv.adapter = adapter
            adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                exception -> Log.w("Read Check" , "Error getting document", exception)
            }
    }

    override fun onResume() {
        super.onResume()
        fetchNews()
    }

    override fun onStart(){
        super.onStart()
        fetchNews()
    }

}

