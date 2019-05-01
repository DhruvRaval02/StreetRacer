package com.example.dhruv.surfaceviewdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    //Code from this program has been used from Beginning Android Games
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;
    int time = 30;
    MediaPlayer mediaPlayer;
    MediaPlayer litPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);

        mediaPlayer = MediaPlayer.create(this, R.raw.butterfly);
        litPlayer = MediaPlayer.create(this, R.raw.lit);
        mediaPlayer.start();


    }

    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }



    //----------------------------GameSurface Below This Line--------------------------
    public class GameSurface extends SurfaceView implements Runnable,SensorEventListener{


        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        boolean isFast = false;
        boolean isScore = true;
        Bitmap ball;
        Bitmap whip;
        Bitmap background;
        Bitmap destroyedWhip;
        int ballX=0;
        int whipDown = 0;
        int whipX = 0;
        int x=200;
        int counter = 0;
        int whipSpeed = 5;
        int score = 0;
        int litCounter = 0;
        Paint paintProperty;

        Canvas canvas;

        int screenWidth;
        int screenHeight;

        public GameSurface(Context context) {
            super(context);
            holder=getHolder();
            ball= BitmapFactory.decodeResource(getResources(),R.drawable.redcar);
            whip = BitmapFactory.decodeResource(getResources(), R.drawable.whip);
            background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            destroyedWhip = BitmapFactory.decodeResource(getResources(), R.drawable.destroyedcar);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;

            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this,accelerometerSensor,sensorManager.SENSOR_DELAY_NORMAL);

            paintProperty= new Paint();
            paintProperty.setTextSize(100);
            paintProperty.setColor(Color.WHITE);

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isFast == false){
                        isFast = true;
                        whipSpeed = 10;
                    }
                    else {
                        isFast = false;
                        whipSpeed = 5;
                    }
                }
            });

            CountDownTimer count = new CountDownTimer(31000, 1000){

                @Override
                public void onTick(long millisUntilFinished) {
                    time--;
                }

                @Override
                public void onFinish() {
                    running = false;
                }
            };
            count.start();

        }

        @Override
        public void run() {

            while (running == true){

                if (holder.getSurface().isValid() == false)
                    continue;

                canvas= holder.lockCanvas();

                canvas.drawBitmap(background, 0, 0, null);

                canvas.drawText(time + "", (screenWidth/4) + 230, 100, paintProperty);

                canvas.drawBitmap(ball, (screenWidth / 2) - ball.getWidth() / 2 + ballX, (screenHeight / 2) - ball.getHeight() + 200, null);

                if(counter == 0){
                    whipX = (int)(Math.random() * (screenWidth - 200) + 200);
                    counter++;

                }
                canvas.drawBitmap(whip, whipX, whipDown, null);
                whipDown+=whipSpeed;
                if(whipDown > screenHeight) {
                    litCounter = 0;
                    ball = BitmapFactory.decodeResource(getResources(), R.drawable.redcar);
                    canvas.drawBitmap(whip, whipX, whipDown, null);
                    whipDown = -50;
                    whipX = (int)((Math.random()* screenWidth)/1.2);
                    isScore = false;
                }

                if(Math.abs(((screenWidth / 2) - ball.getWidth() / 2 + ballX) - whipX) < ball.getWidth() && Math.abs((((screenHeight / 2) - ball.getHeight() + 200)) - whipDown) < ball.getHeight()) {
                    ball = BitmapFactory.decodeResource(getResources(), R.drawable.destroyedcar);
                    isScore = true;
                    if(litCounter == 0){
                        litPlayer.start();
                        litCounter++;
                    }
                }

                if(whipDown > (screenHeight / 2) - ball.getHeight() + 200 && !isScore){
                    score++;
                    isScore = true;
                }

                holder.unlockCanvasAndPost(canvas);

            }

            mediaPlayer.stop();

            Canvas canvasOne = holder.lockCanvas();
            canvasOne.drawRGB(0, 0, 255);
            canvasOne.drawText("GAME OVER", (screenWidth/4), screenHeight/2 - 400, paintProperty);

            canvasOne.drawText("SCORE: " + score, screenWidth/4, screenHeight/2 + 200, paintProperty);
            holder.unlockCanvasAndPost(canvasOne);
            Log.d("TAG", score + "");
        }

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
        }

        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                }
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            int change = (int) -((event.values[0]) * 10);

            if(((screenWidth / 2) - ball.getWidth() / 2 + ballX > 0 && (screenWidth / 2) - ball.getWidth() / 2 + ballX < screenWidth - x) || ((screenWidth / 2) - ball.getWidth() / 2 + ballX <= screenWidth - x && change > 0) ||  ((screenWidth / 2) - ball.getWidth() / 2 + ballX >= 0 && change < 0)){
                ballX += change;
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    //GameSurface
}//Activity
