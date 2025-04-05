package com.example.projetws2;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.projetws2.adapter.EtudiantAdapter;
import com.example.projetws2.beans.Etudiant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListEtudiantActivity extends AppCompatActivity {
    private static final String TAG = "ListEtudiantActivity";
    private ListView listView;
    private RequestQueue requestQueue;
    private List<Etudiant> etudiants;
    private EtudiantAdapter adapter;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView currentImageView;
    private String[] currentPhotoBase64 = new String[1];


    // Remplacez cette URL par l'URL de votre service web
    private String loadUrl = "http://10.0.2.2/TPVOLLEY/php02_1/ws/loadEtudiant.php";
    private String deleteUrl = "http://10.0.2.2/TPVOLLEY/php02_1/ws/deleteEtudiant.php";
    private String updateUrl = "http://10.0.2.2/TPVOLLEY/php02_1/ws/updateEtudiant.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_etudiant);

        listView = findViewById(R.id.listView);
        etudiants = new ArrayList<>();
        adapter = new EtudiantAdapter(this, etudiants);
        listView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        loadEtudiants();
        // Ajout de l'écouteur de clics sur les éléments de la ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Etudiant etudiantSelected = etudiants.get(position);
                showOptionsDialog(etudiantSelected);
            }
        });
    }

    private void loadEtudiants() {
        StringRequest request = new StringRequest(Request.Method.POST, loadUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        try {
                            Type type = new TypeToken<Collection<Etudiant>>(){}.getType();
                            Collection<Etudiant> etudiantsCollection = new Gson().fromJson(response, type);

                            etudiants.clear();
                            etudiants.addAll(etudiantsCollection);
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ListEtudiantActivity.this, "Erreur de parsing JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Erreur de chargement: " + error.getMessage());
                        Toast.makeText(ListEtudiantActivity.this, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(request);
    }
    // Méthode pour afficher le dialogue des options (Modifier ou Supprimer)
    private void showOptionsDialog(final Etudiant etudiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Options pour " + etudiant.getPrenom() + " " + etudiant.getNom());
        builder.setItems(new CharSequence[]{"Modifier", "Supprimer"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Modifier
                        showUpdateDialog(etudiant);
                        break;
                    case 1: // Supprimer
                        showDeleteConfirmationDialog(etudiant);
                        break;
                }
            }
        });
        builder.show();
    }
    // Méthode pour afficher la boîte de dialogue de confirmation de suppression
    private void showDeleteConfirmationDialog(final Etudiant etudiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation de suppression");
        builder.setMessage("Êtes-vous sûr de vouloir supprimer " + etudiant.getPrenom() + " " + etudiant.getNom() + " ?");
        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteEtudiant(etudiant);
            }
        });
        builder.setNegativeButton("Non", null);
        builder.show();
    }
    // Méthode pour afficher la boîte de dialogue de modification
    private void showUpdateDialog(final Etudiant etudiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_etudian, null);
        builder.setView(dialogView);

        final EditText editNom = dialogView.findViewById(R.id.edit_nom);
        final EditText editPrenom = dialogView.findViewById(R.id.edit_prenom);
        final Spinner spinnerVille = dialogView.findViewById(R.id.spinner_ville);
        final RadioButton radioHomme = dialogView.findViewById(R.id.radio_homme);
        final RadioButton radioFemme = dialogView.findViewById(R.id.radio_femme);
        final Button btnDatePicker = dialogView.findViewById(R.id.btn_date_picker);
        final TextView txtDateSelected = dialogView.findViewById(R.id.txt_date_selected);


        // Nouvelles vues pour la photo
        final ImageView photoView = dialogView.findViewById(R.id.photo);
        final Button btnSelectPhoto = dialogView.findViewById(R.id.btnSelectPhoto);

        btnSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Sélectionner une image"), PICK_IMAGE_REQUEST);

                // Sauvegarde temporaire pour y accéder dans onActivityResult
                currentImageView = photoView;

            }
        });

        // Variable pour stocker la photo Base64
        currentPhotoBase64[0] = etudiant.getPhoto();
        // Afficher la photo existante si disponible
        if (etudiant.getPhoto() != null && !etudiant.getPhoto().isEmpty()) {
            byte[] decodedString = Base64.decode(etudiant.getPhoto(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            photoView.setImageBitmap(bitmap);
        }

        // Variable pour stocker la date sélectionnée
        final String[] selectedDate = {etudiant.getDateNaissance()};

        // Remplir les champs avec les données actuelles de l'étudiant
        editNom.setText(etudiant.getNom());
        editPrenom.setText(etudiant.getPrenom());
        txtDateSelected.setText(etudiant.getDateNaissance());

        // Sélectionner la ville dans le spinner
        String[] villes = getResources().getStringArray(R.array.villes);
        for (int i = 0; i < villes.length; i++) {
            if (villes[i].equals(etudiant.getVille())) {
                spinnerVille.setSelection(i);
                break;
            }
        }

        // Sélectionner le sexe
        if (etudiant.getSexe().equals("homme")) {
            radioHomme.setChecked(true);
        } else {
            radioFemme.setChecked(true);
        }

        // Gestionnaire de clics pour le bouton de sélection de date
        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Parse la date existante ou utilise la date actuelle
                Calendar calendar = Calendar.getInstance();
                int year, month, day;

                if (selectedDate[0] != null && !selectedDate[0].isEmpty()) {
                    String[] dateParts = selectedDate[0].split("-");
                    year = Integer.parseInt(dateParts[0]);
                    month = Integer.parseInt(dateParts[1]) - 1; // Les mois commencent à 0 dans Calendar
                    day = Integer.parseInt(dateParts[2]);
                } else {
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH);
                    day = calendar.get(Calendar.DAY_OF_MONTH);
                }

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        ListEtudiantActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                selectedDate[0] = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                txtDateSelected.setText(selectedDate[0]);
                            }
                        },
                        year, month, day);
                datePickerDialog.show();
            }
        });

        builder.setTitle("Modifier étudiant");
        builder.setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Récupérer les nouvelles valeurs
                String nouveauNom = editNom.getText().toString();
                String nouveauPrenom = editPrenom.getText().toString();
                String nouvelleVille = spinnerVille.getSelectedItem().toString();
                String nouveauSexe = radioHomme.isChecked() ? "homme" : "femme";

                // Mettre à jour l'objet étudiant
                etudiant.setNom(nouveauNom);
                etudiant.setPrenom(nouveauPrenom);
                etudiant.setVille(nouvelleVille);
                etudiant.setSexe(nouveauSexe);
                etudiant.setDateNaissance(selectedDate[0]);
                etudiant.setPhoto(currentPhotoBase64[0]);

                // Envoyer la mise à jour au serveur
                updateEtudiant(etudiant);
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    // Méthode pour envoyer la requête de suppression au serveur
    private void deleteEtudiant(final Etudiant etudiant) {
        StringRequest request = new StringRequest(Request.Method.POST, deleteUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Actualiser la liste après la suppression
                            loadEtudiants();
                            Toast.makeText(ListEtudiantActivity.this, "Étudiant supprimé avec succès", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ListEtudiantActivity.this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ListEtudiantActivity.this, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(etudiant.getId()));
                return params;
            }
        };

        requestQueue.add(request);
    }
    // Méthode pour envoyer la requête de mise à jour au serveur
    private void updateEtudiant(final Etudiant etudiant) {
        StringRequest request = new StringRequest(Request.Method.POST, updateUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Actualiser la liste après la mise à jour
                            loadEtudiants();
                            Toast.makeText(ListEtudiantActivity.this, "Étudiant mis à jour avec succès", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ListEtudiantActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ListEtudiantActivity.this, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(etudiant.getId()));
                params.put("nom", etudiant.getNom());
                params.put("prenom", etudiant.getPrenom());
                params.put("ville", etudiant.getVille());
                params.put("sexe", etudiant.getSexe());
                params.put("dateNaissance", etudiant.getDateNaissance());
                params.put("photo", etudiant.getPhoto());
                return params;
            }
        };

        requestQueue.add(request);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                // Convertir l'image sélectionnée en Bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));

                // Afficher dans l'ImageView du popup
                if (currentImageView != null) {
                    currentImageView.setImageBitmap(bitmap);
                }

                // Encoder l'image en Base64
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                byte[] imageBytes = outputStream.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                // Stocker dans le tableau partagé
                if (currentPhotoBase64 != null) {
                    currentPhotoBase64[0] = base64Image;
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
            }
        }
    }

}