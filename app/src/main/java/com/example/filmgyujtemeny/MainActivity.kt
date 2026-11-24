package com.example.filmgyujtemeny

import android.R.attr.name
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.filmgyujtemeny.ui.theme.FilmGyujtemenyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("film_reszletek" ,MODE_PRIVATE)
        setContent {
            MaterialTheme{
                FilmScreen()
            }
        }
    }
}


@Composable
fun FilmScreen() {
    val context = LocalContext.current

    val db = remember {
        FilmDatabaseHelper(context.applicationContext)
    }

    var films by remember {
        mutableStateOf(db.getAllfilms())
    }

    var title by remember {
        mutableStateOf("")
    }

    var genre by remember {
        mutableStateOf("")
    }

    var rating by remember {
        mutableStateOf("")
    }

    var editingId by remember {
        mutableStateOf<Int?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .safeDrawingPadding()
            .padding(16.dp)
    ){
        Text(
            text = "Film Gyűjtemény",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = {title = it},
            label = { Text("Film címe: ")},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = genre,
            onValueChange = {genre = it},
            label = { Text("Film műfaja: ")},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = genre,
            onValueChange = {genre = it},
            label = { Text("Film értékelése: ")},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        Row() {
            Button(
                enabled = title.isNotBlank() && genre.isNotBlank() && (rating.toIntOrNull() ?: 0) in 1 .. 10,
                onClick = {
                    val filmRating = rating.toIntOrNull()
                    if(filmRating != null){
                        val selectedId = editingId
                        if(selectedId == null){
                            db.addNewFilm(title.trim(), genre.trim(), filmRating)
                        }
                        else{
                            db.updateFilm(selectedId, title.trim(), genre.trim(), filmRating)
                        }
                        films = db.getAllfilms()
                        title = ""
                        genre = ""
                        rating = ""
                        editingId = null
                    }
                }
            ) { Text(if(editingId == null) "Hozzáadás" else "Módósítás mentése") }

            if(editingId != null){
                TextButton(
                    onClick = {
                        title = ""
                        genre = ""
                        rating = ""
                        editingId = null
                    }
                ) { Text("Mégse")}
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                text = "FIlmek száma: ${films.size}",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(
                onClick = {
                    films = db.getAllfilms()
                }
            ) { Text("Frissités") }
        }

        Spacer(Modifier.height(12.dp))

        if(films.isEmpty()){
            Text("Még nincs felvett film.")
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(films, key= {it.id} ){
                    film ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ){
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text(
                            text = film.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(){


                            TextButton(
                                onClick = {
                                    editingId = film.id
                                    title = film.title
                                    genre = film.genre
                                    rating = film.rating.toString()
                                }
                            ) {
                                Text("Szerkeztés")
                            }

                            TextButton(
                                onClick = {
                                    db.deleteFilm(film.id)
                                    films = db.getAllfilms()
                                    if(editingId == film.id){
                                        title = ""
                                        genre = ""
                                        rating = ""
                                        editingId = null
                                    }
                                }
                            ) {
                                Text("Törlés", color= Color.Red)
                            }
                        }
                    }
                }
            }
        }

    }
}