package com.example.appgestiontrafico.Servicios;

import com.example.appgestiontrafico.Modelos.MarcadorDoble;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class MarcadorDobleService {

    private CollectionReference mCollection;

    public MarcadorDobleService() {
        mCollection = FirebaseFirestore.getInstance().collection("MarcadorDoble");
    }

    public Task<Void> create(MarcadorDoble marcadorDoble) {
        return mCollection.document().set(marcadorDoble);
    }
    public Task<QuerySnapshot> getAll() {
        return mCollection.get();
    }


}
