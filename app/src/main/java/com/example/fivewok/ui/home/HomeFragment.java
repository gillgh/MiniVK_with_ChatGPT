package com.example.fivewok.ui.home;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fivewok.Login;
import com.example.fivewok.MapActivity;
import com.example.fivewok.R;
import com.example.fivewok.Register;
import com.example.fivewok.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final int REQUEST_IMAGE_GALLERY = 1;
    FirebaseUser user;
    FirebaseAuth auth;
    Button buttonLog, btnEdit, btnSave, btnChgAvatar;
    TextView textName, textSurname, textEmail, textDob;
    EditText chgName, chgSurname, chgEmail, chgDob, chgPassword;
    LinearLayout ll2;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersRef;
    String userId;
    ImageView ivChangeAvatar;
    StorageReference storageRef, imageRef;
    Uri photoUri;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonLog = view.findViewById(R.id.logout);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        textName = view.findViewById(R.id.textName);
        textSurname = view.findViewById(R.id.textSurname);
        textEmail = view.findViewById(R.id.textEmail);
        textDob = view.findViewById(R.id.textDob);
        ivChangeAvatar = view.findViewById(R.id.ivChangeAvatar);

        ll2 = view.findViewById(R.id.ll2);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnSave = view.findViewById(R.id.btn_save);
        btnChgAvatar = view.findViewById(R.id.btn_change_avatar);

        chgName = view.findViewById(R.id.chg_name);
        chgSurname = view.findViewById(R.id.chg_surname);
        chgEmail = view.findViewById(R.id.chg_email);
        chgDob = view.findViewById(R.id.chg_dob);
        chgPassword = view.findViewById(R.id.chg_password);


        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference("users").child(userId);


        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Получение данных из снимка (DataSnapshot) и установка их в соответствующие текстовые поля
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String surname = dataSnapshot.child("surname").getValue(String.class);
                    String email = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
                    String dob = dataSnapshot.child("dob").getValue(String.class);
                    String photoUrl = dataSnapshot.child("photoUrl").getValue(String.class);

                    if (photoUrl != null) {
                        photoUri = Uri.parse(photoUrl);
                    }
                    textName.setText(name);
                    textSurname.setText(surname);
                    textEmail.setText(email);
                    textDob.setText(dob);

                    Picasso.get().load(photoUrl).into(ivChangeAvatar);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Не удалось получить данные из снимка", Toast.LENGTH_SHORT).show();
            }
        });

        if (user == null) {
            Intent intent = new Intent(requireContext(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        }

        buttonLog.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        });

        btnEdit.setOnClickListener(v -> {
            ll2.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
            btnChgAvatar.setVisibility(View.VISIBLE);
            chgEmail.setText(user.getEmail());

        });

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        chgDob.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog;
            DatePickerDialog.OnDateSetListener listener = (view1, year1, month1, dayOfMonth1) -> {
                String date = dayOfMonth1 + "/" + (month1 + 1) + "/" + year1;
                chgDob.setText(date);
            };
            datePickerDialog = new DatePickerDialog(requireContext(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, listener, year, month, dayOfMonth);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            datePickerDialog.show();

        });
        btnChgAvatar.setOnClickListener(v -> getImage());

        btnSave.setOnClickListener(v -> {
            if (chgName.getText().toString().isEmpty() || chgSurname.getText().toString().isEmpty() ||
                    chgEmail.getText().toString().isEmpty() || chgDob.getText().toString().isEmpty()
                    || chgPassword.getText().toString().isEmpty()) {
                Toast.makeText(getActivity(), "Поля не заполнены", Toast.LENGTH_SHORT).show();
            } else {
                ll2.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);
                btnChgAvatar.setVisibility(View.GONE);

                String newEmail = chgEmail.getText().toString().trim();
                String newPassword = chgPassword.getText().toString().trim();
                usersRef.child("name").setValue(chgName.getText().toString());
                usersRef.child("surname").setValue(chgSurname.getText().toString());
                usersRef.child("dob").setValue(chgDob.getText().toString());


                FirebaseUser currentUser = auth.getCurrentUser();

                if (currentUser != null) {
                    currentUser.updateEmail(newEmail)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Обновление успешно завершено
                                    Toast.makeText(getActivity(), "Email успешно обновлен", Toast.LENGTH_SHORT).show();
                                    textEmail.setText(newEmail);

                                } else {
                                    // Обновление не удалось
                                    //Toast.makeText(getActivity(), "Ошибка при обновлении email", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // Пользователь не авторизован
                    Toast.makeText(getActivity(), "Текущий пользователь не авторизован", Toast.LENGTH_SHORT).show();
                }
                assert currentUser != null;
                currentUser.updatePassword(newPassword)
                        .addOnCompleteListener(passwordTask -> {
                            if (passwordTask.isSuccessful()) {
                                // Обновление успешно завершено
                                Toast.makeText(getActivity(), "Пароль успешно обновлен", Toast.LENGTH_SHORT).show();
                            } else {
                                // Обновление не удалось
                                Toast.makeText(getActivity(), "Ошибка при обновлении пароля", Toast.LENGTH_SHORT).show();
                            }
                        });


                if (photoUri != null) {
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("users/" + userId + "/avatar.jpg");
                    storageRef.putFile(photoUri).addOnCompleteListener(profileTask -> {
                        if (profileTask.isSuccessful()) {
                            storageRef.getDownloadUrl().addOnSuccessListener(profileUri -> {
                                String photoUrl = profileUri.toString();
                                // Обновление поля photoUrl у текущего пользователя
                                currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                                                .setPhotoUri(Uri.parse(photoUrl))
                                                .build())
                                        .addOnCompleteListener(updateProfileTask -> {
                                            if (updateProfileTask.isSuccessful()) {
                                                // Обновление успешно завершено
                                                Toast.makeText(getActivity(), "Аватар успешно обновлен", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Обновление не удалось
                                               // Toast.makeText(getActivity(), "Ошибка при обновлении аватара", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                // Обновление поля photoUrl в базе данных
                                usersRef.child(userId).child("photoUrl").setValue(photoUrl)
                                        .addOnCompleteListener(updateDatabaseTask -> {
                                            if (updateDatabaseTask.isSuccessful()) {
                                                // Обновление успешно завершено
                                                Toast.makeText(getActivity(), "Аватар успешно обновлен в базе данных", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Обновление не удалось
                                                Toast.makeText(getActivity(), "Ошибка при обновлении аватара в базе данных", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            });
                        } else {
                            // Произошла ошибка при загрузке фотографии
                            Toast.makeText(getActivity(), "Ошибка при загрузке фотографии.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

                public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Картинка была успешно выбрана из галереи
            photoUri = data.getData();
            Picasso.get().load(photoUri).into(ivChangeAvatar);
        }
    }

    private void getImage(){
        Intent intentChooser = new Intent();
        intentChooser.setType("image/*");
        intentChooser.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intentChooser, "Select Picture"), 1);
    }


}