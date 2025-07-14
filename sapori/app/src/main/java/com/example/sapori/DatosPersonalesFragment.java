package com.example.sapori;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sapori.model.Usuario;
import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatosPersonalesFragment extends Fragment {

    private EditText etNombre, etApellido, etAlias, etEmail;
    private ImageButton btnEditNombre, btnEditApellido, btnEditAlias;
    private LinearLayout btnGuardar;
    private String emailUsuario;

    private ImageView btnback, profileImage;
    private Uri selectedImageUri;

    public DatosPersonalesFragment() {}

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(requireContext())
                            .load(uri)
                            .placeholder(R.drawable.ic_user_placeholder)
                            .into(profileImage);

                    // (Opcional) Subir al servidor
                    subirFotoPerfil(uri);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_datos_personales, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etNombre = view.findViewById(R.id.tvNombre);
        etApellido = view.findViewById(R.id.tvApellido);
        etAlias = view.findViewById(R.id.tvAlias);
        etEmail = view.findViewById(R.id.tvEmail);

        btnEditNombre = view.findViewById(R.id.btnEditNombre);
        btnEditApellido = view.findViewById(R.id.btnEditApellido);
        btnEditAlias = view.findViewById(R.id.btnEditAlias);
        btnback = view.findViewById(R.id.btn_back);

        profileImage = view.findViewById(R.id.profile_image);
        btnGuardar = view.findViewById(R.id.btn_guardar);

        etNombre.setEnabled(false);
        etApellido.setEnabled(false);
        etAlias.setEnabled(false);
        etEmail.setEnabled(false);

        SharedPreferences prefs = requireActivity().getSharedPreferences("sapori_prefs", Context.MODE_PRIVATE);
        emailUsuario = prefs.getString("email_usuario", null);

        if (emailUsuario == null) {
            Toast.makeText(getContext(), "No se encontró el email del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cargar datos del usuario
        ApiService apiService = ApiClient.getApiService();
        Call<Usuario> call = apiService.obtenerUsuarioPorEmail(emailUsuario);

        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario usuario = response.body();
                    etNombre.setText(usuario.getNombre());
                    etApellido.setText(usuario.getApellido());
                    etEmail.setText(usuario.getEmail());
                    etAlias.setText(usuario.getAlias());

                    String urlFotoPerfil = usuario.getFotoPerfil();
                    if (urlFotoPerfil != null && !urlFotoPerfil.isEmpty()) {
                        Glide.with(requireContext())
                                .load(urlFotoPerfil)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Selección de imagen
        profileImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Habilitar edición
        btnEditNombre.setOnClickListener(v -> enableEditField(etNombre));
        btnEditApellido.setOnClickListener(v -> enableEditField(etApellido));
        btnEditAlias.setOnClickListener(v -> enableEditField(etAlias));

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombre.getText().toString().trim();
            String nuevoApellido = etApellido.getText().toString().trim();
            String nuevoAlias = etAlias.getText().toString().trim();

            if (TextUtils.isEmpty(nuevoNombre) || TextUtils.isEmpty(nuevoApellido) || TextUtils.isEmpty(nuevoAlias)) {
                Toast.makeText(getContext(), "Completá todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Usuario usuarioActualizado = new Usuario(nuevoNombre, nuevoApellido, emailUsuario, nuevoAlias);

            ApiService api = ApiClient.getApiService();
            Call<Usuario> updateCall = api.actualizarUsuario(emailUsuario, usuarioActualizado);

            updateCall.enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                    if (response.isSuccessful()) {
                        etNombre.setEnabled(false);
                        etApellido.setEnabled(false);
                        etAlias.setEnabled(false);

                        hideKeyboard();
                        Toast.makeText(getContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();

                        NavController navController = Navigation.findNavController(view);
                        if (navController.getPreviousBackStackEntry() != null) {
                            navController.popBackStack();
                        } else {
                            requireActivity().onBackPressed();
                        }

                    } else {
                        Toast.makeText(getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Usuario> call, Throwable t) {
                    Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnback.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            if (navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });
    }

    private void enableEditField(EditText editText) {
        editText.setEnabled(true);
        editText.requestFocus();
        editText.setSelection(editText.getText().length());

        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        View view = getView();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void subirFotoPerfil(Uri uri) {
        File file = PathUtil.getFileFromUri(requireContext(), uri); // Ya tenés el archivo aquí

        if (file == null || !file.exists()) {
            Toast.makeText(getContext(), "Archivo no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part imagenPart = MultipartBody.Part.createFormData("foto", file.getName(), requestFile);


        ApiService api = ApiClient.getApiService();
        Call<ResponseBody> call = api.subirFotoPerfil(emailUsuario, imagenPart);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Context context = getContext();
                    if (context != null) {
                        Toast.makeText(context, "Foto actualizada correctamente", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    StringBuilder errorMsg = new StringBuilder("Error al subir la imagen. Código: ")
                            .append(response.code());

                    try {
                        if (response.errorBody() != null) {
                            errorMsg.append("\n")
                                    .append(response.errorBody().string());
                        }
                    } catch (IOException e) {
                        errorMsg.append("\nNo se pudo leer el cuerpo de error.");
                        e.printStackTrace();
                    }

                    // Imprimí el error completo en el Logcat
                    Log.e("UPLOAD_ERROR", errorMsg.toString());

                    // Mostralo en un Toast largo
                    Toast.makeText(getContext(), errorMsg.toString(), Toast.LENGTH_LONG).show();
                }
            }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


}
