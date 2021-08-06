package com.example.appgestiontrafico.Servicios;

import com.example.appgestiontrafico.Modelos.Usuario;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UsuarioService {

    private CollectionReference mCollection;

    public UsuarioService(){
        mCollection = FirebaseFirestore.getInstance().collection("Usuarios");
    }

    public Task<Void> create(Usuario usuario) {
        return mCollection.document(usuario.getId()).set(usuario);
    }

    //OBTIENE LA INFORMACION DE UN USUARIO
    public Task<DocumentSnapshot> getUsuario(String id) {
        return mCollection.document(id).get();
    }

}
