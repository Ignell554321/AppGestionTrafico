package com.example.appgestiontrafico.Actividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appgestiontrafico.Modelos.Usuario;
import com.example.appgestiontrafico.R;
import com.example.appgestiontrafico.Servicios.AutenticacionService;
import com.example.appgestiontrafico.Servicios.UsuarioService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dmax.dialog.SpotsDialog;

public class RegistroActivity extends AppCompatActivity implements View.OnClickListener{

    private AutenticacionService autenticacionService;
    private TextView txtNombreCompleto, txtCorreoElectronico, txtPassword;
    private Button btn_registro;
    private UsuarioService usuarioService;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        txtNombreCompleto=findViewById(R.id.txtNombreCompleto);
        txtCorreoElectronico=findViewById(R.id.txtEmail);
        txtPassword=findViewById(R.id.txtPassword);
        btn_registro=findViewById(R.id.btnRegistro);

        autenticacionService=new AutenticacionService();
        usuarioService=new UsuarioService();

        btn_registro.setOnClickListener(this);

        mDialog= new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento...")
                .setCancelable(false).build();

    }

    //FUNCION QUE VALIDA EL REGISTRO DE UN USUARIO
    private void registrar(){

        String nombreUsuario= txtNombreCompleto.getText().toString();
        String correoElectronico=txtCorreoElectronico.getText().toString();
        String clave=txtPassword.getText().toString();

        if(!nombreUsuario.isEmpty() && !correoElectronico.isEmpty() && !clave.isEmpty())
        {
            if(isCorreoElectronicoValido(correoElectronico)){

                    if(clave.length()>=6){

                        crearUsuario(nombreUsuario,correoElectronico,clave);

                    }else{
                        Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                    }

                } else{

                Toast.makeText(this, "Insertaste todos los campos pero el correo electronico no es valido", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this, "Para continuar inserta todos los campos", Toast.LENGTH_SHORT).show();
        }

    }

    //FUNCION QUE CREA UN NUEVO USUARIO
    private void crearUsuario(String nombreUsuario, String correoElectronico, String clave) {
        mDialog.show();
        autenticacionService.registrar(correoElectronico, clave).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        Usuario usuario = new Usuario();
                        usuario.setId(autenticacionService.getIdUsuario());
                        usuario.setCorreoElectronico(correoElectronico);
                        usuario.setNombreCompleto(nombreUsuario);
                        //usuario.setTimestamp(new Date().getTime());
                        usuarioService.create(usuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else {

                                    Toast.makeText(RegistroActivity.this, "No se pudo almacenar el usuario en la base de datos", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                    } else {
                        mDialog.dismiss();
                        Toast.makeText(RegistroActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        //VERIFICAMOS EMAIL VALIDO
        public boolean isCorreoElectronicoValido(String correoElectronico) {
            String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(correoElectronico);
            return matcher.matches();
        }


        @Override
        public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnRegistro:
                registrar();
                break;
        }
    }

}