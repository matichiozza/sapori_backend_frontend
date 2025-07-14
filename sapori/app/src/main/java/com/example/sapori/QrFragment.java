package com.example.sapori;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.sapori.network.ApiClient;
import com.example.sapori.network.ApiService;
import com.example.sapori.model.Asistencia;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrFragment extends Fragment {
    private PreviewView previewView;
    private ExecutorService cameraExecutor;

    private static final String QR_ESPERADO = "sapori123";

    private ApiService apiService;

    private Long alumnoId;
    private Long cursoId;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(getContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr, container, false);

        previewView = view.findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        ImageButton btnAtras = view.findViewById(R.id.btnAtrasQr);
        btnAtras.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack();
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("sapori_prefs", 0);
        alumnoId = prefs.getLong("id_usuario", -1);
        cursoId = -1L;

        apiService = ApiClient.getApiService();

        Log.d("QrFragment", "alumnoId (id_usuario) obtenido: " + alumnoId);

        if (alumnoId == -1) {
            Toast.makeText(getContext(), "No se encontró alumnoId en SharedPreferences", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation_container);
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation_container);
        if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                BarcodeScanner scanner = BarcodeScanning.getClient();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> scanBarcode(imageProxy, scanner));

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void scanBarcode(ImageProxy imageProxy, BarcodeScanner scanner) {
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty()) {
                            for (Barcode barcode : barcodes) {
                                String rawValue = barcode.getRawValue();

                                if (rawValue != null && rawValue.equals(QR_ESPERADO)) {
                                    Toast.makeText(getContext(), "QR válido: " + rawValue, Toast.LENGTH_SHORT).show();

                                    validarFechaYMarcarPresente();

                                    requireActivity().runOnUiThread(() ->
                                            Navigation.findNavController(requireView()).popBackStack()
                                    );
                                } else {
                                    Toast.makeText(getContext(), "QR inválido", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                        }
                    })
                    .addOnFailureListener(Throwable::printStackTrace)
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    private void validarFechaYMarcarPresente() {
        apiService.getCursosPorAlumno(alumnoId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> cursos = response.body();
                    LocalDate fechaActual = LocalDate.now();

                    for (Map<String, Object> curso : cursos) {
                        Object idObj = curso.get("cursoId");
                        if (!(idObj instanceof Number)) continue;

                        long idCurso = ((Number) idObj).longValue();
                        procesarCursoParaAsistencia(curso, idCurso, fechaActual);
                    }

                    if (cursos.isEmpty()) {
                        Toast.makeText(getContext(), "No estás inscripto a ningún curso.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al obtener cursos inscriptos.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red al obtener cursos inscriptos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procesarCursoParaAsistencia(Map<String, Object> curso, long idCurso, LocalDate fechaActual) {
        apiService.getAsistenciasPorAlumnoYCurso(alumnoId, idCurso).enqueue(new Callback<List<Asistencia>>() {
            @Override
            public void onResponse(Call<List<Asistencia>> call, Response<List<Asistencia>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Asistencia> asistencias = response.body();

                    boolean claseHoyExiste = false;
                    boolean asistenciaMarcadaHoy = false;
                    final int[] claseHoy = {-1}; // Array mutable para claseHoy

                    for (Asistencia asistencia : asistencias) {
                        try {
                            LocalDateTime fechaDateTime = LocalDateTime.parse(asistencia.getFecha(), DateTimeFormatter.ISO_DATE_TIME);
                            LocalDate fechaAsistencia = fechaDateTime.toLocalDate();

                            if (fechaAsistencia.isEqual(fechaActual)) {
                                claseHoyExiste = true;
                                claseHoy[0] = asistencia.getClase();
                                if ("Presente".equalsIgnoreCase(asistencia.getAsistencia())) {
                                    asistenciaMarcadaHoy = true;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (!claseHoyExiste) {
                        Log.d("Asistencia", "No hay clase hoy en el curso: " + curso.get("nombre"));
                        return;
                    }

                    if (asistenciaMarcadaHoy) {
                        Toast.makeText(getContext(), "Ya estás marcado como presente hoy en el curso: " + curso.get("nombre"), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String fechaStr = fechaActual.format(DateTimeFormatter.ISO_DATE);
                    String fechaCodificada = "";
                    try {
                        fechaCodificada = URLEncoder.encode(fechaStr, StandardCharsets.UTF_8.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Marcar presente la clase de hoy
                    apiService.marcarPresenteConFecha(alumnoId, idCurso, fechaCodificada).enqueue(new Callback<Asistencia>() {
                        @Override
                        public void onResponse(Call<Asistencia> call2, Response<Asistencia> response2) {
                            if (response2.isSuccessful()) {
                                Toast.makeText(getContext(), "¡Presente marcado con éxito en el curso: " + curso.get("nombre") + "!", Toast.LENGTH_SHORT).show();
                                Log.d("Asistencia", "Marcado presente con éxito.");

                                // Ahora, marcar como ausente las clases anteriores que estén vacías
                                for (Asistencia asistencia : asistencias) {
                                    if (asistencia.getClase() < claseHoy[0] &&
                                            (asistencia.getAsistencia() == null || asistencia.getAsistencia().isEmpty())) {

                                        // Obtener fecha de esa clase para pasarla al endpoint
                                        String fechaClaseStr;
                                        try {
                                            LocalDateTime fechaClaseDateTime = LocalDateTime.parse(asistencia.getFecha(), DateTimeFormatter.ISO_DATE_TIME);
                                            fechaClaseStr = fechaClaseDateTime.toLocalDate().format(DateTimeFormatter.ISO_DATE);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            fechaClaseStr = fechaStr; // fallback a fecha actual si falla
                                        }

                                        String fechaClaseCodificada = "";
                                        try {
                                            fechaClaseCodificada = URLEncoder.encode(fechaClaseStr, StandardCharsets.UTF_8.toString());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        // Llamar a marcarAusenteConFecha para esa clase anterior vacía
                                        apiService.marcarAusenteConFecha(alumnoId, idCurso, fechaClaseCodificada)
                                                .enqueue(new Callback<Asistencia>() {
                                                    @Override
                                                    public void onResponse(Call<Asistencia> call, Response<Asistencia> response) {
                                                        if (response.isSuccessful()) {
                                                            Log.d("Asistencia", "Marcado ausente clase anterior con éxito: clase " + asistencia.getClase());
                                                        } else {
                                                            Log.e("Asistencia", "Error marcando ausente clase anterior: " + response.code() + " - " + response.message());
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Asistencia> call, Throwable t) {
                                                        Log.e("Asistencia", "Fallo marcando ausente clase anterior: " + t.getMessage());
                                                    }
                                                });
                                    }
                                }

                            } else {
                                Toast.makeText(getContext(), "Error al marcar asistencia en el curso: " + curso.get("nombre"), Toast.LENGTH_SHORT).show();
                                Log.e("Asistencia", "Error al marcar asistencia. Código: " + response2.code() + " - " + response2.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<Asistencia> call2, Throwable t) {
                            Toast.makeText(getContext(), "Error al marcar asistencia en el curso: " + curso.get("nombre"), Toast.LENGTH_SHORT).show();
                            Log.e("Asistencia", "Fallo al marcar asistencia: " + t.getMessage());
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "Error al obtener asistencias.", Toast.LENGTH_SHORT).show();
                    Log.e("Asistencia", "Error en la respuesta asistencias. Código: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Asistencia>> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo al obtener asistencias.", Toast.LENGTH_SHORT).show();
                Log.e("Asistencia", "Fallo al obtener asistencias: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
