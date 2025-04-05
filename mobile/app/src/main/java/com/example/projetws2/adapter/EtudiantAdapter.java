package com.example.projetws2.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.projetws2.R;
import com.example.projetws2.beans.Etudiant;

import java.util.List;

public class EtudiantAdapter extends BaseAdapter {
    private List<Etudiant> etudiants;
    private LayoutInflater inflater;

    public EtudiantAdapter(Context context, List<Etudiant> etudiants) {
        this.etudiants = etudiants;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return etudiants.size();
    }

    @Override
    public Object getItem(int position) {
        return etudiants.get(position);
    }

    @Override
    public long getItemId(int position) {
        return etudiants.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_etudiant, null);
            holder = new ViewHolder();
            holder.id = convertView.findViewById(R.id.id);
            holder.nom = convertView.findViewById(R.id.nom);
            holder.prenom = convertView.findViewById(R.id.prenom);
            holder.ville = convertView.findViewById(R.id.ville);
            holder.sexe = convertView.findViewById(R.id.sexe);
            holder.dateNaissance = convertView.findViewById(R.id.dateNaissance);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Etudiant etudiant = etudiants.get(position);
        holder.id.setText(String.valueOf(etudiant.getId()));
        holder.nom.setText(etudiant.getNom());
        holder.prenom.setText(etudiant.getPrenom());
        holder.ville.setText(etudiant.getVille());
        holder.sexe.setText(etudiant.getSexe());
        holder.dateNaissance.setText(etudiant.getDateNaissance());

        return convertView;
    }

    private class ViewHolder {
        TextView id;
        TextView nom;
        TextView prenom;
        TextView ville;
        TextView sexe;
        TextView dateNaissance;
    }
}
