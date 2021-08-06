package com.example.appgestiontrafico.Actividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appgestiontrafico.Modelos.Usuario;
import com.example.appgestiontrafico.R;
import com.example.appgestiontrafico.Servicios.AutenticacionService;
import com.example.appgestiontrafico.Servicios.UsuarioService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import org.w3c.dom.Text;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView jlbl_registro;
    private Button btn_login;
    private FirebaseAuth firebaseAuth;
    private AlertDialog mDialog;
    private TextView txtCorreoElectronico, txtContraseña;
    private AutenticacionService autenticacionService;
    private UsuarioService usuarioService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        jlbl_registro=findViewById(R.id.log_lbl_registro);
        btn_login=findViewById(R.id.btnLogin);
        txtCorreoElectronico=findViewById(R.id.txtEmailLogin);
        txtContraseña=findViewById(R.id.txtPasswordLogin);

        autenticacionService=new AutenticacionService();
        usuarioService=new UsuarioService();

        jlbl_registro.setOnClickListener(this);
        btn_login.setOnClickListener(this);


        mDialog= new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento...")
                .setCancelable(false).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (autenticacionService.getSessionUsuario() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }
    }

    //FUNCION QUE EVALUARA SI EL USUARIO LOGEADO YA SE ENCUENTRA ALMACENADO EN LA BASE DE DATOS
    private void checkUserExist(final String id) {
        usuarioService.getUsuario(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    mDialog.dismiss();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else {
                    Usuario usuario = new Usuario();
                    usuario.setCorreoElectronico(autenticacionService.getCorreoElectronico());
                    usuario.setId(id);
                    usuarioService.create(usuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mDialog.dismiss();
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(LoginActivity.this, "No se pudo almacenar la informacion del usuario", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.log_lbl_registro:
                registrar_usuario();
                break;
            case R.id.btnLogin:
                iniciarSesion();
                break;
        }
    }

    //FUNCION PARA ABRIR EL REGISTRO DE UN NUEVO USUARIO
    private void registrar_usuario() {
        Intent i_registro=new Intent(getApplicationContext(),RegistroActivity.class);
        startActivity(i_registro);
    }

    private void iniciarSesion() {

        String correo_electronico= txtCorreoElectronico.getText().toString().trim();
        String clave=txtContraseña.getText().toString().trim();

        if(!correo_electronico.isEmpty() && !clave.isEmpty())
        {
            mDialog.show();
            autenticacionService.login(correo_electronico, clave).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mDialog.dismiss();
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                    }
                    else {
                        Toast.makeText(LoginActivity.this, "El email o la contraseña que ingresaste no son correctas", Toast.LENGTH_LONG).show();
                    }
                    }
                });

        }else{
            Toast.makeText(LoginActivity.this, "Debes completar los campos para continuar.", Toast.LENGTH_LONG).show();
        }

        }
}