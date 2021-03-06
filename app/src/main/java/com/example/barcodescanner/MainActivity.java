package com.example.barcodescanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.unity3d.player.UnityPlayerActivity;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {


        private SurfaceView surfaceView;
        private BarcodeDetector barcodeDetector;
        private CameraSource cameraSource;
        private static final int REQUEST_CAMERA_PERMISSION = 201;
        private ToneGenerator toneGen1;
        private TextView barcodeText;
        private String barcodeData;

        private FirebaseFirestore db;




        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            surfaceView = findViewById(R.id.surface_view);
            barcodeText = findViewById(R.id.barcode_text);

            Button button = findViewById(R.id.clickbutton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, UnityHolderActivity.class);
                    startActivity(intent);
                }
            });

            FirebaseFirestore db = FirebaseFirestore.getInstance();

           /* CollectionReference citiesRef = db.collection("produkte");
            Query query = citiesRef.whereEqualTo("test", "SUCCESFUL");
            System.out.println("**************************************************************************");
            System.out.println(query);*/


            db.collection("produkte")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    //Log.d(TAG, document.getId() + " => " + document.getData());
                                    System.out.println("**************************************************************************");
                                    System.out.println(document.getData());
                                }
                            } else {
                                //Log.w(TAG, "Error getting documents.", task.getException());
                                System.out.println("**************************************************************************");
                                System.out.println(task.getException());
                            }
                        }
                    });

            initialiseDetectorsAndSources();
        }

        private void initialiseDetectorsAndSources() {

            //Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

            barcodeDetector = new BarcodeDetector.Builder(this)
                    .setBarcodeFormats(Barcode.ALL_FORMATS)
                    .build();

            cameraSource = new CameraSource.Builder(this, barcodeDetector)
                    .setRequestedPreviewSize(1920, 1080)
                    .setAutoFocusEnabled(true) //you should add this feature
                    .build();

            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            cameraSource.start(surfaceView.getHolder());
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, new
                                    String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });


            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {
                    // Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections) {
                    final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                    if (barcodes.size() != 0) {


                        barcodeText.post(new Runnable() {

                            @Override
                            public void run() {

                                if (barcodes.valueAt(0).email != null) {
                                    barcodeText.removeCallbacks(null);
                                    barcodeData = barcodes.valueAt(0).email.address;
                                    barcodeText.setText(barcodeData);
                                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                                } else {

                                    barcodeData = barcodes.valueAt(0).displayValue;
                                    barcodeText.setText(barcodeData);
                                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);

                                }
                            }
                        });

                    }
                }
            });
        }


        @Override
        protected void onPause() {
            super.onPause();
            getSupportActionBar().hide();
            cameraSource.release();
        }

        @Override
        protected void onResume() {
            super.onResume();
            getSupportActionBar().hide();
            initialiseDetectorsAndSources();
        }


}
