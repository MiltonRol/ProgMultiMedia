package dam2.mmo.firstapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlin.random.Random

class Trilero3 : ComponentActivity() {
    private lateinit var start: Button
    private lateinit var cubo1: ImageView
    private lateinit var cubo2: ImageView
    private lateinit var cubo3: ImageView

    private var playing = false

    private lateinit var mensaje: TextView







    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modo_3_cocos)

        start = findViewById<Button>(R.id.start)
        mensaje = findViewById<TextView>(R.id.mensaje)
        start.setOnClickListener {
                if (playing == false) {
                    mensaje.visibility = View.INVISIBLE
                    playing = true

                    playing()
                }else{
                    mensaje.visibility = View.VISIBLE
                    playing = false

                    notPlaying()

                }
            }

    }

    fun resultado(seleccion:Int, posicion: Int){
        if (seleccion != -1){
            if(seleccion == posicion){
                when (posicion){
                    1 -> {cubo1.setImageResource(R.drawable.levantaok)}
                    2 -> {cubo2.setImageResource(R.drawable.levantaok)}
                    3 -> {cubo3.setImageResource(R.drawable.levantaok)}
                }
                mensaje.setText("Has Ganado")
            }else{
                when (seleccion){
                    1 -> {cubo1.setImageResource(R.drawable.levantafail)}
                    2 -> {cubo2.setImageResource(R.drawable.levantafail)}
                    3 -> {cubo3.setImageResource(R.drawable.levantafail)}
                }
                when (posicion){
                    1 -> {cubo1.setImageResource(R.drawable.levantaok)}
                    2 -> {cubo2.setImageResource(R.drawable.levantaok)}
                    3 -> {cubo3.setImageResource(R.drawable.levantaok)}
                }
                mensaje.setText("Has Perdido")
            }
        }
        start.visibility = View.VISIBLE

    }

    fun playing(){
        mensaje.visibility = View.VISIBLE
        start.visibility=View.INVISIBLE


        val posicion = Random.nextInt(1,3)
        var seleccion = -1

        cubo1 = findViewById<ImageView>(R.id.cubilete1)
        cubo2 = findViewById<ImageView>(R.id.cubilete2)
        cubo3 = findViewById<ImageView>(R.id.cubilete3)


        cubo1.setImageResource(R.drawable.cubilete)
        cubo2.setImageResource(R.drawable.cubilete)
        cubo3.setImageResource(R.drawable.cubilete)

        cubo1.setOnClickListener {
            seleccion = 1
            resultado(seleccion,posicion)
            mensaje.visibility = View.VISIBLE}
        cubo2.setOnClickListener {
            seleccion = 2
            resultado(seleccion,posicion)
            mensaje.visibility = View.VISIBLE}
        cubo3.setOnClickListener {
            seleccion = 3
            resultado(seleccion,posicion)
            mensaje.visibility = View.VISIBLE}
        start.setOnClickListener { notPlaying() }
    }

    fun notPlaying(){
        start.visibility=View.VISIBLE
        start.setText("Get Scammed")
        mensaje.visibility = View.INVISIBLE


        cubo1 = findViewById<ImageView>(R.id.cubilete1)
        cubo2 = findViewById<ImageView>(R.id.cubilete2)
        cubo3 = findViewById<ImageView>(R.id.cubilete3)


        cubo1.setImageResource(R.drawable.cubilete)
        cubo2.setImageResource(R.drawable.cubilete)
        cubo3.setImageResource(R.drawable.cubilete)
        start.setOnClickListener { playing() }


    }
}