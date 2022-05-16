package com.example.imagefromgallery;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView image;
    private Button gallery, camera;
    private SwitchCompat mySwitch;

    private final ActivityResultLauncher<Intent> launcher;

    public MainActivity() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getData() != null) {

                            // Voglio salvare l'immagine in una directory
                            // se la directory non esiste, la creo
                            final File file = new File(getFilesDir().getAbsolutePath() + "/images/image.jpeg");
                            if (!Files.exists(Paths.get(getFilesDir().getAbsolutePath() + "/images"))) {
                                try (FileOutputStream fos = new FileOutputStream(file)) {
                                    Files.createDirectory(Paths.get(getFilesDir().getAbsolutePath() + "/images"));
                                    final Bitmap selectedImage;
                                    final Uri imageUri = result.getData().getData();
                                    if (imageUri == null) {
                                        // arrivo dalla camera
                                        selectedImage = (Bitmap) result.getData().getExtras().get("data");
                                    } else {
                                        // arrivo dalla gallery
                                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                                        selectedImage = BitmapFactory.decodeStream(imageStream);
                                    }

                                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                                    image.setImageBitmap(selectedImage);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        widgetBinding();

        gallery.setOnClickListener(this);
        camera.setOnClickListener(this);

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bitmap imageSelected = BitmapFactory.decodeFile(getFilesDir().getAbsolutePath() + "/images/image.jpeg");
        image.setImageBitmap(imageSelected);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == gallery.getId()) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            launcher.launch(intent);
        } else if (view.getId() == camera.getId()) {
            richiedoPermessi();
        }
    }

    private void richiedoPermessi() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            launcher.launch(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                launcher.launch(intent);
            } else {
                Snackbar.make(image, "Occorrono i permessi per usare la fotocamera", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void widgetBinding() {
        image = findViewById(R.id.image);
        gallery = findViewById(R.id.gallery);
        camera = findViewById(R.id.camera);
        mySwitch = findViewById(R.id.mySwitch);
    }
}