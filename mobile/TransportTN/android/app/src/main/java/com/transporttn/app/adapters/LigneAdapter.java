package com.transporttn.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.transporttn.app.R;
import com.transporttn.app.models.Ligne;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter RecyclerView pour l'affichage des lignes (Bus ou Taxi Collectif).
 * Affiche : itineraire, heure de depart, prix, places restantes (cahier des charges 2.3/2.4).
 */
public class LigneAdapter extends RecyclerView.Adapter<LigneAdapter.LigneViewHolder> {

    public interface OnLigneClickListener {
        void onLigneClick(Ligne ligne);
    }

    private List<Ligne> lignes = new ArrayList<>();
    private final OnLigneClickListener listener;

    public LigneAdapter(OnLigneClickListener listener) {
        this.listener = listener;
    }

    public void setLignes(List<Ligne> lignes) {
        this.lignes = lignes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LigneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ligne, parent, false);
        return new LigneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LigneViewHolder holder, int position) {
        Ligne ligne = lignes.get(position);
        holder.routeText.setText(ligne.getDeparture() + " → " + ligne.getDestination());
        holder.timeText.setText(ligne.getDepartTime());
        holder.priceText.setText(String.format("%.2f DT", ligne.getPrice()));
        holder.seatsText.setText(ligne.getSeatsAvailable() + " places restantes");
        holder.itemView.setOnClickListener(v -> listener.onLigneClick(ligne));
    }

    @Override
    public int getItemCount() {
        return lignes.size();
    }

    static class LigneViewHolder extends RecyclerView.ViewHolder {
        TextView routeText, timeText, priceText, seatsText;

        LigneViewHolder(@NonNull View itemView) {
            super(itemView);
            routeText = itemView.findViewById(R.id.routeText);
            timeText = itemView.findViewById(R.id.timeText);
            priceText = itemView.findViewById(R.id.priceText);
            seatsText = itemView.findViewById(R.id.seatsText);
        }
    }
}
