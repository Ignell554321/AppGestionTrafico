package com.example.appgestiontrafico.Servicios;

import com.example.appgestiontrafico.Modelos.Marcador;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class MarcadorService {

    private CollectionReference mCollection;

    public MarcadorService() {
        mCollection = FirebaseFirestore.getInstance().collection("Marcador");
    }

    public Task<Void> create(Marcador marcador) {
        return mCollection.document().set(marcador);
    }

    public Query getAll(Boolean activo) {
        return mCollection.whereArrayContains("activo", activo);
    }

    public Task<QuerySnapshot> getAll2() {
        return mCollection.get();
    }

    public Query getAllNombre(String nombre) {
        return mCollection.whereArrayContains("nombre", nombre);
    }

}
