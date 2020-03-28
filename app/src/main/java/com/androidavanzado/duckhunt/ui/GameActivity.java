package com.androidavanzado.duckhunt.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidavanzado.duckhunt.R;
import com.androidavanzado.duckhunt.common.Constantes;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    TextView tvCounterDucks, tvTimer, tvNick;
    ImageView ivDuck;
    int counter = 0;
    int anchoPantalla;
    int altoPantall;
    Random aleatorio;
    boolean gameOver = false;
    String id, nick;
    FirebaseFirestore db;
    MediaPlayer duck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        db = FirebaseFirestore.getInstance();

        duck = MediaPlayer.create(this, R.raw.duck);

        initViewComponents();
        eventos();
        initPantallaSize();
        moveDuck();
        initCuentaAtras();

    }

    private void initCuentaAtras() {

        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                long segundosRestantes = millisUntilFinished / 1000;
                tvTimer.setText(segundosRestantes + "s");
            }

            public void onFinish() {
                tvTimer.setText("0s");
                gameOver = true;
                mostrarDialogoGameOver();
                saveResultFirestore();
            }
        }.start();

    }

    private void saveResultFirestore() {

        db.collection("users")
                .document(id)
                .update(
                  "ducks", counter
                );

    }

    private void mostrarDialogoGameOver() {

        // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Has conseguido cazar " + counter + " patos")
                .setTitle("Game Over");

        // Add the buttons
        builder.setPositiveButton("Reiniciar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                counter = 0;
                tvCounterDucks.setText("0");
                gameOver = false;
                initCuentaAtras();
                moveDuck();
            }
        });
        builder.setNegativeButton("Ver Ranking", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Intent i = new Intent(GameActivity.this, RankingActivity.class);
                startActivity(i);
            }
        });

        // 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
        AlertDialog dialog = builder.create();

        // 4. Mostrar el dialogo
        dialog.show();

    }

    private void initPantallaSize() {

        // 1. Obtener el tamaño de la pantalla del dispositivo
        // en el que estamos ejecutando la app
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        anchoPantalla = size.x;
        altoPantall = size.y;

        // 2. Inicializamos el objeto para generar números aleatorios
        aleatorio = new Random();

    }

    private void initViewComponents() {

        tvCounterDucks = findViewById(R.id.textViewCounter);
        tvTimer = findViewById(R.id.textViewTimer);
        tvNick = findViewById(R.id.textViewNick);
        ivDuck = findViewById(R.id.imageViewDuck);

        // Cambiar fuente
        Typeface typeface = Typeface.createFromAsset(getAssets(), "pixel.ttf");
        tvCounterDucks.setTypeface(typeface);
        tvTimer.setTypeface(typeface);
        tvNick.setTypeface(typeface);

        // Extras: obtener nick y setear en TextView
        Bundle extras = getIntent().getExtras();
        nick = extras.getString(Constantes.EXTRA_NICK);
        id = extras.getString(Constantes.EXTRA_ID);
        tvNick.setText(nick);

    }

    private void eventos() {

        ivDuck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!gameOver) {
                    counter++;
                    tvCounterDucks.setText(String.valueOf(counter));

                    ivDuck.setImageResource(R.drawable.duck_clicked);

                    // Método que realiza las acciones despues de clicar en el pato
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            duck.start();
                            ivDuck.setImageResource(R.drawable.duck);
                            moveDuck();
                        }
                    }, 250);
                }
            }
        });

    }

    private void moveDuck() {

        int min = 0;
        int maximoX = anchoPantalla - ivDuck.getWidth();
        int maximoY = altoPantall - ivDuck.getHeight();

        // Generamos 2 números aleatorios, uno para la coordenada
        // x y otro para la coordenada y.
        int randomX = aleatorio.nextInt(((maximoX - min) + 1) + min);
        int randomY = aleatorio.nextInt(((maximoY - min) + 1) + min);

        // Utilizamos los números aleatorios para mover el pato a esa posición
        ivDuck.setX(randomX);
        ivDuck.setY(randomY);

    }

}
