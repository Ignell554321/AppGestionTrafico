package com.example.appgestiontrafico.Actividades;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.appgestiontrafico.R;
import com.example.appgestiontrafico.Servicios.AutenticacionService;
import com.example.appgestiontrafico.Servicios.UsuarioService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilActivity extends AppCompatActivity implements View.OnClickListener{

    LinearLayout linearLayoutEditarPerfil;
    View mView;

    private TextView jtxtViewNombreUsuario;
    private TextView jtxtViewCorreoElectronico;
    private ImageView imgFondo;
    private CircleImageView circleImagePerfil;
    private UsuarioService usuarioService;
    private AutenticacionService autenticacionService;

    private String nombreUsuario = "";
    private String correoElectronico = "";
    private String telefono = "";
    private String imagenPerfil = "";
    private String imagenFondo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        usuarioService=new UsuarioService();
        autenticacionService=new AutenticacionService();

        jtxtViewNombreUsuario=findViewById(R.id.jtxtViewNombreUsuario);
        jtxtViewCorreoElectronico=findViewById(R.id.jtxtViewCorreoElectronico);
        imgFondo=findViewById(R.id.imgFondo);
        circleImagePerfil=findViewById(R.id.circleImagePerfil);
        linearLayoutEditarPerfil.setOnClickListener(this);
        imgFondo.setOnClickListener(this);
        getUsuario();
    }

    private void getUsuario(){

        usuarioService.getUsuario(autenticacionService.getIdUsuario()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {

                    if (documentSnapshot.contains("nombreCompleto")) {
                        nombreUsuario = documentSnapshot.getString("nombreCompleto");
                        jtxtViewNombreUsuario.setText(nombreUsuario);
                    }

                    if (documentSnapshot.contains("correoElectronico")) {
                        correoElectronico = documentSnapshot.getString("correoElectronico");
                        jtxtViewCorreoElectronico.setText(correoElectronico);
                    }

                    /*
                    if (documentSnapshot.contains("imagenPerfil")) {
                        imagenPerfil = documentSnapshot.getString("imagenPerfil");
                        if (imagenPerfil != null) {
                            if (!imagenPerfil.isEmpty()) {
                                Picasso.with(getApplicationContext()).load(imagenPerfil).into(circleImagePerfil);
                            }
                        }
                    }
                    if (documentSnapshot.contains("imagenFondo")) {
                        imagenFondo = documentSnapshot.getString("imagenFondo");
                        if (imagenFondo != null) {
                            if (!imagenFondo.isEmpty()) {
                                Picasso.with(getApplicationContext()).load(imagenFondo).into(imgFondo);
                            }
                        }
                    }*/


                }
            }
        });

    }

    private void editarImagenPerfil(){

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.circleImagePerfil:
                editarImagenPerfil();
                break;
        }
    }
}