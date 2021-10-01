package com.example.appgestiontrafico.Actividades;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appgestiontrafico.Modelos.Marcador;
import com.example.appgestiontrafico.Modelos.MarcadorDoble;
import com.example.appgestiontrafico.R;
import com.example.appgestiontrafico.Servicios.AutenticacionService;
import com.example.appgestiontrafico.Servicios.GoogleApiService;
import com.example.appgestiontrafico.Servicios.MarcadorDobleService;
import com.example.appgestiontrafico.Servicios.MarcadorService;
import com.example.appgestiontrafico.Utils.DecodePoints;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private Marker marker;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;
    private static int AUTOCOMPLETE_REQUEST_CODE = 3;

    private Button btn_detener_gps;
    private Button crearMarcador;
    private Button btn_crear_ruta;
    private boolean conectado;

    private MarcadorService marcadorService;
    private MarcadorDobleService marcadorDobleService;
    private AutocompleteSupportFragment autocomplete;
    private AutocompleteSupportFragment autocompleteDestino;
    private PlacesClient places;

    private String origen;
    private String destino;
    private LatLng originLatLng;
    private LatLng destinoLatLng;
    private LatLng mCurrentLatLng;

    private List<LatLng> listaLatLng=new ArrayList<>();
    private AutenticacionService autenticacionService;

    private GoogleApiService googleApiService;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private boolean marcadorSimpleTrafico=false;
    private int contadorMarcadorSimpleTrafico=0;
    private List<LatLng> listaMarcadoresTrafico=new ArrayList<>();


    LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {

            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if(marker!=null)
                    {
                        marker.remove();
                    }
                    marker=mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                            .title("Tu posicion")
                    );

                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));

                    limitarBusquedas();
                }
            }
        }

    };


    private void  mostrarModalMarcadorTrafico(LatLng latLng){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_marcador_trafico, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();
        Button btn_MarcadorTraficoSimple= view.findViewById(R.id.btn_marcador_trafico_simple);
        Button btn_MarcadorTraficoDoble=view.findViewById(R.id.btn_marcador_trafico_doble);

        btn_MarcadorTraficoSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                guardarMarcadorSimple(latLng,"Tarfico",dialog);

            }
        });

        btn_MarcadorTraficoDoble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                contadorMarcadorSimpleTrafico++;
                listaMarcadoresTrafico.add(latLng);
                Toast.makeText(MainActivity.this, "Selecciona el lugar final", Toast.LENGTH_LONG).show();

            }
        });

    }


    private void guardarMarcadorSimple(LatLng latLng, String nombreMarcador, Dialog dialog){

        //HORA INICIO
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();

        //HORA ACTUALIZACION
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); //FechaBase
        calendar.add(Calendar.MINUTE, 10); //minutosASumar +10
        Date fechaSalida = calendar.getTime(); //la fecha sumada.

        Marcador  marcador=new Marcador();
        marcador.setNombre(nombreMarcador);
        marcador.setActivo(true);
        marcador.setLatitud(latLng.latitude);
        marcador.setLongitud(latLng.longitude);
        marcador.setHoraInicio(dateFormat.format(date));
        marcador.setHoraActualizacion(dateFormat.format(fechaSalida));

        marcadorService.create(marcador).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "Marcador Almacenado correctamente", Toast.LENGTH_SHORT).show();
                    obtenerMarcadores();

                }else{
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "No se pudo almacenar el marcador", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void guardarMarcadorDoble( String nombreMarcador){

        //HORA INICIO
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();

        //HORA ACTUALIZACION
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); //FechaBase
        calendar.add(Calendar.MINUTE, 10); //minutosASumar +10
        Date fechaSalida = calendar.getTime(); //la fecha sumada.

        MarcadorDoble marcadorDoble= new MarcadorDoble();

        marcadorDoble.setLatitudMarcador1(listaMarcadoresTrafico.get(0).latitude);
        marcadorDoble.setLongitudMarcador1(listaMarcadoresTrafico.get(0).longitude);
        marcadorDoble.setLatitudMarcador2(listaMarcadoresTrafico.get(1).latitude);
        marcadorDoble.setLongitudMarcador2(listaMarcadoresTrafico.get(1).longitude);
        marcadorDoble.setHoraActualizacion(dateFormat.format(fechaSalida));
        marcadorDoble.setActivo(true);
        marcadorDoble.setNombre(nombreMarcador);

        marcadorDobleService.create(marcadorDoble).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(MainActivity.this, "Marcadores Almacenados correctamente", Toast.LENGTH_SHORT).show();
                    obtenerMarcadores();

                }else{

                    Toast.makeText(MainActivity.this, "No se pudo almacenar el marcador", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    private void mostrarDialogoPersonalizado(LatLng latLng) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_personalizado, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();
        TextView txt = view.findViewById(R.id.text_dialog);
        txt.setText("TIPO DE MARCADOR");

        Button btnTrafico = view.findViewById(R.id.btnTrafico);
        Button btnAccidente = view.findViewById(R.id.btnAccidente);
        Button btnObras = view.findViewById(R.id.btnObras);
        Button btn_cancelar=view.findViewById(R.id.btn_cancelar);

        btnTrafico.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(contadorMarcadorSimpleTrafico==0)
                {
                    mostrarModalMarcadorTrafico(latLng);
                }
                dialog.dismiss();

            }

        });

        btnAccidente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //HORA INICIO
                DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                Date date = new Date();

                //HORA ACTUALIZACION
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date); //FechaBase
                calendar.add(Calendar.MINUTE, 10); //minutosASumar +10
                Date fechaSalida = calendar.getTime(); //la fecha sumada.

                Marcador  marcador=new Marcador();
                marcador.setNombre("Accidente");
                marcador.setActivo(true);
                marcador.setLatitud(latLng.latitude);
                marcador.setLongitud(latLng.longitude);
                marcador.setHoraInicio(dateFormat.format(date));
                marcador.setHoraActualizacion(dateFormat.format(fechaSalida));

                marcadorService.create(marcador).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            dialog.dismiss();
                            obtenerMarcadores();
                            Toast.makeText(MainActivity.this, "Marcador Almacenado correctamente", Toast.LENGTH_SHORT).show();

                        }else{
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "No se pudo almacenar el marcador", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }

        });

        btnObras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //HORA INICIO
                DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                Date date = new Date();

                //HORA ACTUALIZACION
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date); //FechaBase
                calendar.add(Calendar.MINUTE, 10); //minutosASumar +10
                Date fechaSalida = calendar.getTime(); //la fecha sumada.

                Marcador  marcador=new Marcador();
                marcador.setNombre("Obras");
                marcador.setActivo(true);
                marcador.setLatitud(latLng.latitude);
                marcador.setLongitud(latLng.longitude);
                marcador.setHoraInicio(dateFormat.format(date));
                marcador.setHoraActualizacion(dateFormat.format(fechaSalida));

                marcadorService.create(marcador).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            dialog.dismiss();
                            obtenerMarcadores();
                            Toast.makeText(MainActivity.this, "Marcador Almacenado correctamente", Toast.LENGTH_SHORT).show();

                        }else{
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "No se pudo almacenar el marcador", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });

        btn_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        } else if (requestCode == SETTINGS_REQUEST_CODE && !gpsActived()) {
            showAlertDialogGPS();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    } else {
                        showAlertDialogGPS();
                    }

                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }


    private void showAlertDialogGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }


    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }

        return isActive;
    }


    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                   // btn_detener_gps.setText("Desconectar");
                    conectado=true;
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                } else {
                    showAlertDialogGPS();
                }

            } else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            } else {
                showAlertDialogGPS();
            }

        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }

                        })
                        .create()
                        .show();
            }

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        marcadorService=new MarcadorService();
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        btn_crear_ruta=findViewById(R.id.btn_Ruta);
        crearMarcador=findViewById(R.id.btn_Marcador);
        autenticacionService=new AutenticacionService();
        marcadorDobleService=new MarcadorDobleService();
        googleApiService=new GoogleApiService(MainActivity.this);

        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(),getResources().getString(R.string.google_maps_key));
        }

        places=Places.createClient(this);

        autocomplete=(AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placesAutocompleteOrigin);
        autocompleteDestino=(AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placesAutocompleteDestino);

        autocomplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteDestino.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));

        autocomplete.setHint("Lugar de Origen");
        autocompleteDestino.setHint("Lugar de Destino");


        autocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                origen=place.getName();
                originLatLng=place.getLatLng();

                Log.d("PLACE","Namce "+origen);
                Log.d("LATITUD","Namce "+originLatLng.latitude);
                Log.d("LONGITUD","Namce "+originLatLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        autocompleteDestino.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                destino=place.getName();
                destinoLatLng=place.getLatLng();

                Log.d("PLACE","Namce "+destino);
                Log.d("LATITUD","Namce "+destinoLatLng.latitude);
                Log.d("LONGITUD","Namce "+destinoLatLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        crearMarcador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoPersonalizado(mCurrentLatLng);

            }
        });

        btn_crear_ruta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearRuta();
            }
        });
        actualizarMarcadores();
    }

    private void actualizarMarcadores(){
        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(), "probando hilo cada 10 seg", Toast.LENGTH_SHORT).show();
                obtenerMarcadores();
                obtenerMarcadoresDobles();
                handler.postDelayed(this,5000);//se ejecutara cada 5 segundos
            }
        },5000);//empezara a ejecutarse después de 5 milisegundos
    }

    private void crearRuta(){
        if(originLatLng!=null && destinoLatLng!=null){
            Intent intent=new Intent(MainActivity.this,RutaActivity.class);
            intent.putExtra("origin_lat", originLatLng.latitude);
            intent.putExtra("origin_lng", originLatLng.longitude);
            intent.putExtra("destination_lat", destinoLatLng.latitude);
            intent.putExtra("destination_lng", destinoLatLng.longitude);
            intent.putExtra("origen", origen);
            intent.putExtra("destino", destino);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Debe seleccionar el lugar de recogida y el destino", Toast.LENGTH_SHORT).show();
        }
    }

    private void limitarBusquedas(){
        LatLng northSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000, 0);
        LatLng southSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000, 180);
        autocomplete.setCountry("PER");
        autocomplete.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
        autocompleteDestino.setCountry("PER");
        autocompleteDestino.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
    }

    private void obtenerMarcadoresDobles(){
        marcadorDobleService.getAll().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>(){

            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {

                    if(document.getBoolean("activo")){

                        MarkerOptions marcador1 = new MarkerOptions();
                        MarkerOptions marcador2 = new MarkerOptions();
                        LatLng latLongMarcador1 = new LatLng(document.getDouble("latitudMarcador1"), document.getDouble("longitudMarcador1"));
                        LatLng latLongMarcador2 = new LatLng(document.getDouble("latitudMarcador2"), document.getDouble("longitudMarcador2"));
                        marcador1.position(latLongMarcador1);
                        marcador2.position(latLongMarcador2);
                        marcador1.title(document.getString("nombre"));
                        marcador2.title(document.getString("nombre"));

                        if(document.getString("nombre").equals("Trafico")){
                            marcador1.icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_car));
                            marcador2.icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_car));
                            drawRoute(latLongMarcador1,latLongMarcador2,1);
                        }
                        if(document.getString("nombre").equals("Obras")){
                            marcador1.icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_obras));
                            marcador2.icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_obras));
                            drawRoute(latLongMarcador1,latLongMarcador2,2);
                        }
                        if(document.getString("nombre").equals("Accidente")){
                            marcador1.icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_accidente));
                            marcador2.icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_accidente));
                            drawRoute(latLongMarcador1,latLongMarcador2,3);
                        }

                        mMap.addMarker(marcador1);
                        mMap.addMarker(marcador2);



                    }
                }
            }
        });
    }

    private void obtenerMarcadores(){

        marcadorService.getAll().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mMap.clear();
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {

                    if(document.getBoolean("activo")){

                        Log.d("ERROR",document.getDouble("latitud")+"");
                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng marcador = new LatLng(document.getDouble("latitud"), document.getDouble("longitud"));

                        markerOptions.position(marcador);
                        markerOptions.title(document.getString("nombre"));
                        if(document.getString("nombre").equals("Trafico")){
                            markerOptions.icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_car));
                        }
                        if(document.getString("nombre").equals("Obras")){
                            markerOptions.icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_obras));
                        }
                        if(document.getString("nombre").equals("Accidente")){
                            markerOptions.icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_accidente));
                        }

                        mMap.addMarker(markerOptions);

                    }


                }

            }
        });
    }

    private void drawRoute(LatLng mOriginLatLng, LatLng mDestinationLatLng, int tipoRuta) {
        googleApiService.getDirections(mOriginLatLng, mDestinationLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {

                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mPolylineList = DecodePoints.decodePoly(points);
                    mPolylineOptions = new PolylineOptions();

                    if(tipoRuta==1){
                        mPolylineOptions.color(Color.BLUE);
                    }else{
                        mPolylineOptions.color(Color.DKGRAY);
                    }

                    mPolylineOptions.width(8f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    mMap.addPolyline(mPolylineOptions);

                } catch (Exception e) {
                    Log.d("Error", "Error encontrado " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        obtenerMarcadores();
        obtenerMarcadoresDobles();

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                if(contadorMarcadorSimpleTrafico==0)
                {
                    mostrarDialogoPersonalizado(latLng);
                    Log.d("listLatLng","contador: "+contadorMarcadorSimpleTrafico);

                }else{

                    Log.d("listLatLng","contador: "+contadorMarcadorSimpleTrafico);
                    contadorMarcadorSimpleTrafico=0;
                    listaMarcadoresTrafico.add(latLng);
                    guardarMarcadorDoble("Trafico");

                    for (LatLng latlng:listaMarcadoresTrafico) {

                        markerOptions = new MarkerOptions();
                        markerOptions.position(latlng);
                        markerOptions.title(latlng.latitude + " : " + latlng.longitude);
                        googleMap.addMarker(markerOptions);
                    }

                   drawRoute(listaMarcadoresTrafico.get(0),listaMarcadoresTrafico.get(1),1);
                    listaMarcadoresTrafico.clear();

                    //Toast.makeText(MainActivity.this, "Marcadores Almacenados", Toast.LENGTH_LONG).show();
                    Log.d("listLatLng",listaMarcadoresTrafico.toString());


                }

                obtenerMarcadores();
                obtenerMarcadoresDobles();


            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);

        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
        startLocation();

    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.driver_menu,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_logout){
            logout();
        }

        if(item.getItemId()==R.id.perfilusuario)
        {
            perfilUsuario();
        }
        return super.onOptionsItemSelected(item);
    }

    private void perfilUsuario(){
        Intent intent=new Intent(MainActivity.this,PerfilActivity.class);
        startActivity(intent);
    }

    private void logout(){
        autenticacionService.CerrarSesion();
        Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Toast.makeText(getApplicationContext(),"Has cerrado sesion",Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

}