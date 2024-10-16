package com.example.czas;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView countdownText;
    private Button startButton, stopButton;
    private Handler handler;
    private Runnable runnable;
    private boolean isCounting = false;
    private long eventTime;
    private long remainingTime = 0;  // Czas pozostały do wydarzenia

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countdownText = findViewById(R.id.countdownText);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        handler = new Handler(Looper.getMainLooper());//to pobiera i zwraca obiekt typu looper jest to coś powiązane z głównym wątkiem aplikacji

        // Data wydarzenia (np. Nowy Rok)
        eventTime = getEventTime("01/01/2025 00:00:00");
        //Po zmianie orientacji ekranu aktywność w Androidzie jest niszczona
        // i tworzona od nowa, co powoduje utratę stanu, w tym zatrzymanie licznika.
        // Przywrócenie stanu po zmianie orientacji
        if (savedInstanceState != null) {
            remainingTime = savedInstanceState.getLong("remainingTime");
            isCounting = savedInstanceState.getBoolean("isCounting");
            if (isCounting) {
                startCountdown();
            }
        }
        //onSaveInstanceState: Przechowuje dane, takie jak pozostały czas odliczania
        // i stan licznika (czy licznik działa).
        //onRestoreInstanceState: Przywraca dane po zmianie orientacji ekranu, aby
        // licznik mógł kontynuować odliczanie.
        //remainingTime: Przechowuje czas, jaki pozostał do wydarzenia, aby
        // po zmianie orientacji telefonu odliczanie mogło być kontynuowane od miejsca, w którym zostało przerwane.
        // Runnable do aktualizacji zegara
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isCounting) {
                    long currentTime = System.currentTimeMillis();
                    long timeLeft = eventTime - currentTime;

                    if (timeLeft > 0) {
                        long days = timeLeft / (1000 * 60 * 60 * 24);
                        long hours = (timeLeft / (1000 * 60 * 60)) % 24;
                        long minutes = (timeLeft / (1000 * 60)) % 60;
                        long seconds = (timeLeft / 1000) % 60;

                        String timeLeftFormatted = String.format(Locale.getDefault(),
                                "%d dni %02d:%02d:%02d", days, hours, minutes, seconds);
                        countdownText.setText(timeLeftFormatted);

                        // Uruchomienie ponownej aktualizacji po 1 sekundzie
                        handler.postDelayed(this, 1000);
                    } else {
                        countdownText.setText("Wydarzenie już się odbyło!");
                        stopCounting();
                    }
                }
            }
        };

        // Start odliczania
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCounting) {
                    isCounting = true;
                    startCountdown();
                }
            }
        });

        // Stop odliczania
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCounting();
            }
        });
    }

    private long getEventTime(String eventDate) {//służy do zamiany daty w formie tekstowej (np. "01/01/2025 00:00:00") na czas w postaci liczby całkowitej.
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        try {
            Date event = dateFormat.parse(eventDate);
            return event != null ? event.getTime() : System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
    }

    private void startCountdown() {
        handler.post(runnable);
    }

    private void stopCounting() {
        isCounting = false;
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("remainingTime", eventTime - System.currentTimeMillis());
        outState.putBoolean("isCounting", isCounting);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        remainingTime = savedInstanceState.getLong("remainingTime");
        isCounting = savedInstanceState.getBoolean("isCounting");
        if (isCounting) {
            startCountdown();
        }
    }
}

