package net.brach.android.flux.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.brach.android.flux.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private final List<String> bitmaps = Arrays.asList(
            "bitmaps/flower_1.png",
            "bitmaps/flower_2.png",
            "bitmaps/flower_3.png",
            "bitmaps/flower_4.png",
            "bitmaps/flower_5.png"
    );

    private Random random;
    private View to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        random = new Random();
        to = findViewById(R.id.end);

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init();
            }
        });

        init();
    }

    private void init() {
        ViewGroup flowers = (ViewGroup) findViewById(R.id.flowers);
        for (int i = 0; i < flowers.getChildCount(); i++) {
            Button button = (Button) flowers.getChildAt(i);

            button.setAlpha(1);
            button.setClickable(true);

            int count = 1 + random.nextInt(100);
            final Flux flux = new Flux.FluxBuilder(this)
                    .from(button)
                    .to(to)
                    .number(count)
                    .assets(bitmaps, 15, 65)
                    .duration(1000, 1500)
                    .build();

            button.setText(String.valueOf(count));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flux.start();
                    v.setOnClickListener(null);
                }
            });
        }

        ViewGroup circles = (ViewGroup) findViewById(R.id.circles);
        for (int i = 0; i < circles.getChildCount(); i++) {
            Button button = (Button) circles.getChildAt(i);

            button.setAlpha(1);
            button.setClickable(true);

            int count = 1 + random.nextInt(100);
            final Flux flux = new Flux.FluxBuilder(this)
                    .from(button)
                    .to(to)
                    .number(count)
                    .circle(7, 12)
                    .duration(1000, 1500)
                    .build();

            button.setText(String.valueOf(count));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flux.start();
                    v.setOnClickListener(null);
                }
            });
        }
    }
}
