package com.example.sapori;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sapori.model.Receta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecetasViewModel extends ViewModel {
    // LiveData para lista de recetas calculadas (como tenías)
    private MutableLiveData<List<Map<String, Object>>> recetasCalculadas = new MutableLiveData<>(new ArrayList<>());

    // LiveData para la receta original
    private MutableLiveData<Receta> recetaOriginal = new MutableLiveData<>();

    // Getters
    public LiveData<List<Map<String, Object>>> getRecetasCalculadas() {
        return recetasCalculadas;
    }

    public LiveData<Receta> getRecetaOriginal() {
        return recetaOriginal;
    }

    // Setters / Actualizadores
    public void agregarRecetaCalculada(Map<String, Object> nuevaReceta) {
        List<Map<String, Object>> listaActual = recetasCalculadas.getValue();
        if (listaActual == null) {
            listaActual = new ArrayList<>();
        }
        listaActual.add(nuevaReceta);
        recetasCalculadas.setValue(listaActual);
    }

    public void eliminarRecetaCalculada(Map<String, Object> recetaAEliminar) {
        List<Map<String, Object>> listaActual = recetasCalculadas.getValue();
        if (listaActual != null) {
            listaActual.remove(recetaAEliminar);
            recetasCalculadas.setValue(listaActual);
        }
    }

    public void setRecetaOriginal(Receta receta) {
        recetaOriginal.setValue(receta);
    }
}
