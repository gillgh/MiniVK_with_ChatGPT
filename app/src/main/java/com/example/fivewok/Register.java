package com.example.fivewok;

import static java.lang.String.valueOf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fivewok.user.User;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Objects;

public class Register extends AppCompatActivity {
    private static final int REQUEST_IMAGE_GALLERY = 1; // Константа для идентификации запроса выбора картинки из галереи
    private static final int REQUEST_IMAGE_CAMERA = 2; // Константа для идентификации запроса сделать снимок с камеры

    TextInputEditText editTextEmail, editTextPassword, editTextName, editTextSurname, editTextCheckPassword, editTextDob;
    Button buttonReg, btnPict, bntTakePict;
    String userId;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    FirebaseDatabase db;
    DatabaseReference users;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    Uri photoUri;
    ImageView ivAvatar;


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);
        editTextName = findViewById(R.id.name);
        editTextSurname = findViewById(R.id.surname);
        editTextCheckPassword = findViewById(R.id.check_password);
        editTextDob = findViewById(R.id.date_of_birth);
        btnPict = findViewById(R.id.btn_picture);
        bntTakePict = findViewById(R.id.btn_take_picture);
        ivAvatar = findViewById(R.id.ivAvatar);


        photoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.no_avatar);

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        editTextDob.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog;
            DatePickerDialog.OnDateSetListener listener = (view, year1, month1, dayOfMonth1) -> {
                String date = dayOfMonth1 + "." + (month1 + 1) + "." + year1;
                editTextDob.setText(date);
            };
            datePickerDialog = new DatePickerDialog(Register.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, listener, year, month, dayOfMonth);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            datePickerDialog.show();

        });

        btnPict.setOnClickListener(v -> getImage());

        bntTakePict.setOnClickListener(v -> openCamera());

        buttonReg.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            startProgressBarTimer();
            String email = valueOf(editTextEmail.getText());
            String password = valueOf(editTextPassword.getText());
            String name = valueOf(editTextName.getText());
            String surname = valueOf(editTextSurname.getText());
            String dob = valueOf(editTextDob.getText());
            String checkPass = valueOf(editTextCheckPassword.getText());



            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(Register.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(name))  {
            Toast.makeText(Register.this, "Please enter name ", Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(surname))  {
            Toast.makeText(Register.this, "Please enter surname", Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(dob))  {
            Toast.makeText(Register.this, "Please enter date of birth", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!TextUtils.equals(password, checkPass)) {
                Toast.makeText(Register.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }


            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            db = FirebaseDatabase.getInstance();
                            userId = mAuth.getCurrentUser().getUid();

                            users = db.getReference("users");
                            User user = new User();

                            user.setName(name);
                            user.setSurname(surname);
                            user.setEmail(email);
                            user.setDob(dob);
                            user.setPassword(password);

                            users.child(userId).setValue(user);

                            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("users/" + userId + "/avatar.jpg");
                            storageRef.putFile(photoUri).addOnCompleteListener(profileTask -> {
                                if (profileTask.isSuccessful()) {
                                    storageRef.getDownloadUrl().addOnSuccessListener(profileUri -> {
                                        String photoUrl = profileUri.toString();
                                        users.child(userId).child("photoUrl").setValue(photoUrl);
                                    });
                                } else{
                                    // Произошла ошибка при загрузке фотографии
                                    Toast.makeText(Register.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();

                                }
                            });






                            /*
                            StorageReference imageRef = storageRef.child("users/" + userId + "/avatar.jpg");
                            UploadTask uploadTask = imageRef.putFile(imageUri);


                            uploadTask.addOnCompleteListener(profileTask -> {

                                if (profileTask.isSuccessful()) {

                                    // Фотография успешно загружена в Firebase Storage
                                    // Вы можете получить URL загруженной фотографии для сохранения его в базе данных или использования в вашем приложении
                                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        users.child(userId).child("image").setValue(uri.toString());
                                    });
                                        StorageReference storageReference = FirebaseStorage.getInstance()
                                                .getReference().child("users/" + users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()) + "/avatar.jpg");
                                        storageReference.putFile(imageUri).addOnCompleteListener(task1 -> {
                                            users.child(userId).child("photoUrl").setValue(task.toString());
                                        });
                                } else {
                                    // Произошла ошибка при загрузке фотографии
                                    Toast.makeText(Register.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                                }
                            });


                                */
                            Toast.makeText(Register.this, "Account created successfully",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(Register.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    });
        });


    }

    private void startProgressBarTimer() {
        Handler handler = new Handler();
        handler.postDelayed(() -> progressBar.setVisibility(View.GONE), 2500);
    }
    private void getImage(){
        Intent intentChooser = new Intent();
        intentChooser.setType("image/*");
        intentChooser.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intentChooser, "Select Picture"), 1);
    }
    private void openCamera() {
        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAMERA);
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Картинка была успешно выбрана из галереи
            showToast("Image selected from gallery");
            ivAvatar.setImageURI(data.getData());
            photoUri = data.getData();
           // uploadImage(imageUri);
        } else if (requestCode == REQUEST_IMAGE_CAMERA && resultCode == RESULT_OK && data != null) {
            // Снимок с камеры был успешно сделан
            showToast("Image captured from camera");
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            ivAvatar.setImageBitmap(imageBitmap);
            photoUri = getImageUri(imageBitmap);
            //uploadImage(imageUri);
        } else {
            // Пользователь не выбрал картинку или произошла ошибка
            showToast("Image selection canceled");
        }


    }

   /*private void uploadImage(Uri imageUri) {
        if (imageUri != null && users != null) {

            userId = mAuth.getCurrentUser().getUid();


        }
        else{
            Toast.makeText(Register.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
            imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.no_avatar);
        }
    }*/
    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Image Title", null);
        return Uri.parse(path);
    }





    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}