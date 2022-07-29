package com.example.bioface;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.text.DecimalFormat;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_IMAGE_CAPTURE = 300;
    Button cameraButton;
    Button galleryButton;
    FirebaseVisionImage image;
    FirebaseVisionFaceDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        galleryButton = findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_PICK);

                    }
                }
        );

        cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);

                        if (intent.resolveActivity(
                                getPackageManager())
                                != null) {
                            startActivityForResult(
                                    intent, REQUEST_IMAGE_CAPTURE);
                        } else {
                            Toast
                                    .makeText(
                                            MainActivity.this,
                                            "Something went wrong",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode,
                data);
        if (requestCode == REQUEST_IMAGE_CAPTURE
                && resultCode == RESULT_OK) {
            Bundle extra = data.getExtras();
            Bitmap bitmap = (Bitmap) extra.get("data");
            detectFace(bitmap);
        }
    }

    private void detectFace(Bitmap bitmap) {
        FirebaseVisionFaceDetectorOptions options
                = new FirebaseVisionFaceDetectorOptions
                .Builder()
                .setPerformanceMode(
                        FirebaseVisionFaceDetectorOptions
                                .ACCURATE)
                .setLandmarkMode(
                        FirebaseVisionFaceDetectorOptions
                                .ALL_LANDMARKS)
                .setClassificationMode(
                        FirebaseVisionFaceDetectorOptions
                                .ALL_CLASSIFICATIONS)
                .build();

        try {
            image = FirebaseVisionImage.fromBitmap(bitmap);
            detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);
        } catch (Exception e) {
            e.printStackTrace();
        }

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(
                            List<FirebaseVisionFace>
                                    firebaseVisionFaces) {
                        String resultText = "";
                        int i = 1;
                        DecimalFormat df = new DecimalFormat("#.00");
                        for (FirebaseVisionFace face :
                                firebaseVisionFaces) {
                            resultText
                                    = resultText
                                    .concat("\n\nFACE NUMBER " + i )
                                    .concat(
                                            "\nSmile:    "
                                                    + (df.format(face.getSmilingProbability()
                                                    * 100))
                                                    + "%")
                                    .concat(
                                            "\nLeft Eye:  "
                                                    + (df.format(face.getLeftEyeOpenProbability()
                                                    * 100))
                                                    + "%")
                                    .concat(
                                            "\nRight Eye: "
                                                    + (df.format(face.getRightEyeOpenProbability()
                                                    * 100))
                                                    + "%");
                            i++;
                        }

                        if (firebaseVisionFaces.size() == 0) {
                            Toast
                                    .makeText(MainActivity.this,
                                            "NO FACE DETECT",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString(
                                    FaceDetection.RESULT_TEXT,
                                    resultText);
                            DialogFragment resultDialog
                                    = new FaceResult();
                            resultDialog.setArguments(bundle);
                            resultDialog.setCancelable(true);
                            resultDialog.show(
                                    getSupportFragmentManager(),
                                    FaceDetection.RESULT_DIALOG);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast
                                .makeText(
                                        MainActivity.this,
                                        "Oops, Something went wrong",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    public void about(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }

    }
}