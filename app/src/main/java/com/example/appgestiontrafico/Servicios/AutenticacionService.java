package com.example.appgestiontrafico.Servicios;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AutenticacionService {

    private FirebaseAuth mAuth;

    public AutenticacionService(){
        mAuth = FirebaseAuth.getInstance();
    }

    public Task<AuthResult> registrar(String correoElectronico, String clave) {
        return mAuth.createUserWithEmailAndPassword(correoElectronico, clave);
    }

    //AUTENTICACION POR CORREO ELECTRONICO
    public Task<AuthResult> login(String correoElectronico, String clave) {
        return mAuth.signInWithEmailAndPassword(correoElectronico, clave);
    }

    //DEVUELVE EL ID DEL USUARIO AUTENTICADO
    public String getIdUsuario() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        else {
            return null;
        }
    }

    public String getCorreoElectronico() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getEmail();
        }
        else {
            return null;
        }
    }

    public FirebaseUser getSessionUsuario() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser();
        }
        else {
            return null;
        }
    }

    public void CerrarSesion() {
        if (mAuth != null) {
            mAuth.signOut();
        }
    }
}
