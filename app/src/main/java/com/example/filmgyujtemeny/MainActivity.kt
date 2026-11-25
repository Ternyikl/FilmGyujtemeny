package com.example.filmgyujtemeny

import android.R
import android.R.attr.name
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.filmgyujtemeny.ui.theme.FilmGyujtemenyTheme
import kotlin.jvm.java

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("film_reszletek" ,MODE_PRIVATE)
        setContent {

            var userName by remember {
                mutableStateOf(
                    prefs.getString("userName","") ?: ""
                )}

            var darkMode by remember {
                mutableStateOf(prefs.getBoolean("darkMode",false))
            }


            MaterialTheme(
                colorScheme = if(darkMode) darkColorScheme() else lightColorScheme()
            ){
                filmScreen(
                    userName = userName,
                    darkMode = darkMode,
                    onUserNameChange = {
                        newName ->
                        userName = newName
                        prefs.edit().putString("userName", newName).apply()
                    },
                    onDarkModeChange = { enabled ->
                        darkMode = enabled
                        prefs.edit().putBoolean("darkMode", enabled).apply()
                    }
                )
            }
        }
    }
}


@Composable
fun filmScreen(
    userName: String,
    darkMode: Boolean,
    onUserNameChange: (String) -> Unit,
    onDarkModeChange: (Boolean) -> Unit
) {
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

    var showNameDialog by remember {
        mutableStateOf(userName.isBlank())
    }

    var nameInput by remember {
        mutableStateOf(userName)
    }

    var filmToDeleteId by remember {
        mutableStateOf<Int?>(null)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var sortMode by remember {
        mutableStateOf(0)
    }

    val displayedFilms = films
        .filter { it.title.contains(searchQuery.trim(), ignoreCase = true) }
        .let { list ->
            when (sortMode) {
                1 -> list.sortedByDescending { it.rating }
                2 -> list.sortedBy { it.rating }
                else -> list
            }
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

        if(userName.isNotBlank()){
                Text(
                    text = "Szia, ${userName}!\nÜdvözlünk a filmgyüjteményben"
                )
            }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        nameInput = userName
                        showNameDialog = true
                    }
                ) { Text("Név módosítása") }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sötét mód")
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = darkMode,
                        onCheckedChange = onDarkModeChange
                    )
                }
            }
        }

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
            value = rating,
            onValueChange = {rating = it},
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filmek száma: ${films.size}",
                style = MaterialTheme.typography.titleLarge
            )
            Row {
                TextButton(
                    onClick = { sortMode = (sortMode + 1) % 3 }
                ) {
                    Text(
                        when (sortMode) {
                            1 -> "Értékelés ↓"
                            2 -> "Értékelés ↑"
                            else -> "Rendezés"
                        }
                    )
                }
                TextButton(
                    onClick = { films = db.getAllfilms() }
                ) { Text("Frissités") }
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Keresés cím alapján") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        if(films.isEmpty()){
            Text("Még nincs felvett film.")
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayedFilms, key= {it.id} ){
                    film ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable{
                        val intent = Intent(
                            context,
                            FilmDetailsActivity::class.java
                        )

                        intent.putExtra(
                            "title",
                            film.title
                        )
                        intent.putExtra(
                            "genre",
                            film.genre
                        )
                        intent.putExtra(
                            "rating",
                            film.rating
                        )

                        context.startActivity(intent)

                    }
                ){
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "   " + film.title,
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
                                onClick = { filmToDeleteId = film.id }
                            ) {
                                Text("Törlés", color = Color.Red)
                            }
                        }
                    }
                }
            }
        }
        if(showNameDialog){
            AlertDialog(
                onDismissRequest = {
                    if(userName.isNotBlank()){
                        showNameDialog = false
                    }
                },
                title = {
                    Text("Felhasználó név")
                },
                text = {
                    Column() {
                        Text("Add meg a neved!")
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = {
                                nameInput = it
                            },
                            label = {
                                Text("Neved")
                            }
                        )
                    }
                },
                    confirmButton = {
                        TextButton(
                            enabled = nameInput.isNotBlank(),
                            onClick = {
                                onUserNameChange(
                                    nameInput.trim()
                                )
                                showNameDialog = false
                            }

                        ) { Text("Mentés")}
                    },
                dismissButton = {
                    if(userName.isNotBlank()){
                        TextButton(
                            onClick = {
                                showNameDialog = false
                            }
                        ) {  Text("Mégse")}
                    }
                }
            )
        }
        filmToDeleteId?.let { deleteId ->
            val filmTitle = films.firstOrNull { it.id == deleteId }?.title ?: ""

            AlertDialog(
                onDismissRequest = { filmToDeleteId = null },
                title = { Text("Film törlése") },
                text = { Text("Biztosan törölni szeretnéd a(z) \"$filmTitle\" filmet?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            db.deleteFilm(deleteId)
                            films = db.getAllfilms()
                            if (editingId == deleteId) {
                                title = ""
                                genre = ""
                                rating = ""
                                editingId = null
                            }
                            filmToDeleteId = null
                        }
                    ) { Text("Törlés", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(
                        onClick = { filmToDeleteId = null }
                    ) { Text("Mégse") }
                }
            )
        }
    }
}