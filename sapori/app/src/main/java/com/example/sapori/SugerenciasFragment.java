package com.example.sapori;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SugerenciasFragment extends Fragment {

    private RecyclerView recyclerView;
    private SugerenciasAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sugerencias, container, false);

        recyclerView = root.findViewById(R.id.rv_sugerencias_alias);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);

        adapter = new SugerenciasAdapter();
        recyclerView.setAdapter(adapter);

        List<String> sugerenciasOriginales = Arrays.asList(
                "Aquí hay 5 alias alternativos para 'alias puesto'",
                "alias 1",
                "alias 2",
                "alias 3"
        );

        adapter.setSugerencias(sugerenciasOriginales);

        return root;
    }

    private static class SugerenciasAdapter extends RecyclerView.Adapter<SugerenciasAdapter.SugerenciaViewHolder> {

        private List<String> sugerencias = new ArrayList<>();

        public void setSugerencias(List<String> nuevasSugerencias) {
            System.out.println(">>> setSugerencias called");
            if (nuevasSugerencias == null) {
                this.sugerencias.clear();
                System.out.println("SugerenciasAdapter: lista vacía");
            } else {
                System.out.println("SugerenciasAdapter: lista recibida:");
                List<String> filtradas = new ArrayList<>();
                for (String s : nuevasSugerencias) {
                    System.out.println(" - " + s);
                    if (s != null && !s.startsWith("Aquí hay")) {
                        filtradas.add(s);
                    }
                }
                this.sugerencias = filtradas;
            }
            notifyDataSetChanged();
            System.out.println(">>> setSugerencias finished with size: " + this.sugerencias.size());
        }



        @NonNull
        @Override
        public SugerenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sugerencia, parent, false);
            return new SugerenciaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SugerenciaViewHolder holder, int position) {
            holder.tvSugerencia.setText(sugerencias.get(position));
        }

        @Override
        public int getItemCount() {
            return sugerencias.size();
        }

        static class SugerenciaViewHolder extends RecyclerView.ViewHolder {
            TextView tvSugerencia;

            public SugerenciaViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSugerencia = itemView.findViewById(R.id.tvSugerencia);
            }
        }
    }
}