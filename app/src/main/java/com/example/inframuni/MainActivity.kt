package com.example.inframuni

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.Projection
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var isSatellite = false
    private lateinit var db: AppDatabase
    private var currentArea: Area? = null
    private var points = mutableListOf<GeoPoint>()
    private var polygon: Polygon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(8.0)
        mapView.controller.setCenter(GeoPoint(15.7835, -90.2308)) // Centro en Guatemala

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "area-database"
        ).build()

        val fabMenu = findViewById<FloatingActionButton>(R.id.fab_menu)
        fabMenu.setOnClickListener {
            val popup = android.widget.PopupMenu(this, fabMenu)
            popup.menu.add(0, 1, 1, "Cambiar vista: " + if (isSatellite) "Calles" else "Satélite")
            popup.menu.add(0, 2, 2, "Generar nuevo mapa")
            popup.menu.add(0, 3, 3, "Inventario áreas")
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> toggleView()
                    2 -> generateNewMap()
                    3 -> inventoryAreas()
                }
                true
            }
            popup.show()
        }

        // Cargar la última área si existe
        thread {
            val areas = db.areaDao().getAll()
            if (areas.isNotEmpty()) {
                currentArea = areas.last()
                runOnUiThread {
                    drawPolygon(currentArea!!.points)
                    mapView.controller.setCenter(currentArea!!.points[0])
                    mapView.invalidate()
                }
            }
        }
    }

    private fun toggleView() {
        isSatellite = !isSatellite
        if (isSatellite) {
            mapView.setTileSource(TileSourceFactory.USGS_SAT)
        } else {
            mapView.setTileSource(TileSourceFactory.MAPNIK)
        }
        mapView.invalidate()
    }

    private fun generateNewMap() {
        points.clear()
        polygon?.let { mapView.overlays.remove(it) }
        polygon = null
        Toast.makeText(this, "Toca el mapa para agregar 4 puntos", Toast.LENGTH_LONG).show()

        mapView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP && points.size < 4) {
                val projection: Projection = mapView.projection
                val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                points.add(geoPoint)
                if (points.size == 4) {
                    drawPolygon(points)
                    saveArea()
                    mapView.setOnTouchListener(null)
                }
                true
            } else {
                false
            }
        }
    }

    private fun drawPolygon(points: List<GeoPoint>) {
        polygon = Polygon().apply {
            this.points = points + points[0] // Cerrar el polígono
            fillColor = 0x550000FF // Azul semi-transparente
            strokeColor = 0xFF0000FF
            strokeWidth = 5f
        }
        mapView.overlays.add(polygon)
        mapView.invalidate()
    }

    private fun saveArea() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ingresa el nombre del área")
        val input = EditText(this)
        builder.setView(input)
        builder.setPositiveButton("OK") { dialog, which ->
            val name = input.text.toString()
            if (name.isNotEmpty()) {
                val area = Area(name = name, points = points.toList())
                thread {
                    db.areaDao().insert(area)
                    currentArea = area
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Área guardada", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        builder.show()
    }

    private fun inventoryAreas() {
        if (currentArea == null) {
            Toast.makeText(this, "No hay área definida", Toast.LENGTH_SHORT).show()
            return
        }
        // Mostrar mapa del área actual (última guardada)
        mapView.controller.setCenter(currentArea!!.points[0])
        mapView.controller.setZoom(12.0)
        mapView.invalidate()

        // Menú con opciones no implementadas
        val popup = android.widget.PopupMenu(this, findViewById(R.id.fab_menu))
        popup.menu.add(0, 4, 4, "Agregar punto de interés (No implementado)")
        popup.menu.add(0, 5, 5, "Agregar línea (No implementado)")
        popup.menu.add(0, 6, 6, "Agregar polígono (No implementado)")
        popup.setOnMenuItemClickListener { item ->
            Toast.makeText(this, item.title.toString(), Toast.LENGTH_SHORT).show()
            true
        }
        popup.show()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}