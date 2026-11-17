package dam2.mmo.firstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dam2.mmo.firstapp.ui.theme.FirstAppTheme

class ComposeTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirstAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = Modifier.fillMaxSize(),
    )
}

@Preview
@Composable
fun trileroGame() {
    var counter by remember { mutableIntStateOf(0) }
    var textBoton by remember { mutableStateOf(" ${counter}") }

    // Lista de cubos OBSERVABLE: mutableStateListOf avisa a Compose cuando se hace add/remove,
    // y la pantalla se redibuja sola. Antes usabas mutableStateOf(listOf(...)): también vale,
    // pero obliga a crear una lista nueva y reasignarla cada vez. Con esta basta con .add().
    val cubos = remember {
        mutableStateListOf(
            R.drawable.cubilete,
            R.drawable.cubilete,
            R.drawable.cubilete
        )
    }
    var indice by remember { mutableStateOf(0) }

    // Función local que añade un cubo. Errores que tenía la versión anterior:
    //  1) `fun addCubos(index: Int) = { ... }` devolvía una LAMBDA sin ejecutarla (por el `=`),
    //     así que al hacer clic no pasaba nada. Aquí el cuerpo va con llaves normales `{ }`.
    //  2) Estaba escrita DESPUÉS de usarse y dentro de otro bloque mal cerrado.
    //  3) El parámetro `index` no se usaba; añadir un cubo al final no necesita posición.
    fun addCubo() {
        cubos.add(R.drawable.cubilete) // el nuevo cubo se añade al final y Compose lo dibuja

    }
    fun getIndex(valIndex:Int) {indice = valIndex+1}


    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.fondo),
                contentScale = ContentScale.Crop
            )
    ) {
        // Fila centrada en la pantalla (align del Box) con separación entre cubos.
        // CAMBIO: el contenedor exterior pasa de Row a Column. Como ahora hay VARIAS filas
        // (chunked(3) trocea la lista de 3 en 3), las filas deben apilarse en vertical,
        // y eso lo hace una Column. Una Row dentro de otra Row las pondría en horizontal.
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val totalRows : Int = 5

            // Los cubos salen SOLO de la lista: si cambia, se redibuja todo.
            cubos.chunked(3).forEachIndexed { rowIndex, rowCubes ->
                    // CAMBIO: se quita fillMaxSize() de la fila interior. Con él, la primera fila
                    // ocupaba TODA la pantalla y empujaba al resto fuera de la vista.
                if (rowIndex < totalRows) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ){

                    rowCubes.forEachIndexed { columIndex, imageId ->
                        val realIndex = rowIndex * 3 + columIndex

                            Image(
                                painter = painterResource(id = imageId),
                                contentDescription = "Cubo $realIndex",
                                modifier = Modifier
                                    .size(90.dp)
                                    .clickable {
                                        getIndex(realIndex)
                                    } // al pulsar cualquier cubo se añade otro
                            )
                        }
                    }
                }
            }
        }

        // Botón Start abajo y centrado (sin cambios respecto a antes).
        Button(
            onClick = {
                counter++
                textBoton = counter.toString()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        ) { Text(text = "Start ${indice}")
        }

    } // <- ERROR DE SINTAXIS ARREGLADO: faltaba cerrar el Box. Sin esta llave, el `}` de abajo
      //    cerraba el Box y la función trileroGame se quedaba abierta ("Missing '}'").
      //    Regla: cada `{` necesita su `}`; el sangrado de Android Studio (Ctrl+Alt+L) ayuda a verlo.
}

// Estaba anidada dentro de trileroGame; un @Composable de preview debe ir a nivel de archivo.
//@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FirstAppTheme {
        Greeting("Android")
    }
}

