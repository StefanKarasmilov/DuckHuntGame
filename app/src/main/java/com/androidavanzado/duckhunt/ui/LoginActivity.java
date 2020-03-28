package com.androidavanzado.duckhunt.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.androidavanzado.duckhunt.R;
import com.androidavanzado.duckhunt.common.Constantes;
import com.androidavanzado.duckhunt.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    EditText etNick;
    Button btnStrat;
    String nick;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Instanciar la conexión a Firestore
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        etNick = findViewById(R.id.editTextNick);
        btnStrat = findViewById(R.id.buttonStart);

        // Cambiar fuente
        Typeface typeface = Typeface.createFromAsset(getAssets(), "pixel.ttf");
        etNick.setTypeface(typeface);
        btnStrat.setTypeface(typeface);

        // Eventos: evetos click
        btnStrat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nick = etNick.getText().toString();

                if(nick.isEmpty()){
                    etNick.setError("El nickname es obligatorio");
                }else if(nick.length() < 3) {
                    etNick.setError("Debe tener al menos 3 caracteres");
                }
                else{
                    addNickAndStart();
                }
                
            }
        });

    }

    private void addNickAndStart() {

        db.collection("users").whereEqualTo("nick", nick.toLowerCase())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size() > 0) {
                            etNick.setError("El nick no está disponible");
                        } else {
                            addNickToFirestore();
                        }
                    }
                });

    }

    private void addNickToFirestore() {

        User nuevoUsuario = new User(nick, 0);

        db.collection("users")
                .add(nuevoUsuario)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        etNick.setText("");
                        Intent i = new Intent(LoginActivity.this, GameActivity.class);

                        i.putExtra(Constantes.EXTRA_NICK, nick);
                        i.putExtra(Constantes.EXTRA_ID, documentReference.getId());

                        startActivity(i);
                    }
                });

    }
}
