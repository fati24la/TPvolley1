package com.example.projetws2;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.example.projetws2.beans.Etudiant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AddEtudiant extends AppCompatActivity implements View.OnClickListener {

    private EditText nom;
    private EditText prenom;
    private Spinner ville;
    private RadioButton m;
    private RadioButton f;
    private Button add;
    private Button btnDatePicker;
    private TextView txtDateNaissance;
    private String selectedDate = ""; // Pour stocker la date sélectionnée
    private Button listButton;
    private ImageView photo;
    private Button btnSelectPhoto;
    private String base64Image = "";

    private static final int PICK_IMAGE_REQUEST = 1;

    RequestQueue requestQueue;
    String insertUrl = "http://10.0.2.2/TPVOLLEY/php02_1/ws/createEtudiant.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_etudiant);

        nom = (EditText) findViewById(R.id.nom);
        prenom = (EditText) findViewById(R.id.prenom);
        ville = (Spinner) findViewById(R.id.ville);
        add = (Button) findViewById(R.id.add);
        listButton = (Button) findViewById(R.id.list_button);
        m = (RadioButton) findViewById(R.id.m);
        f = (RadioButton) findViewById(R.id.f);
        btnDatePicker = (Button) findViewById(R.id.btnDatePicker);
        txtDateNaissance = (TextView) findViewById(R.id.txtDateNaissance);

        // Nouvelles vues pour la photo
        photo = (ImageView) findViewById(R.id.photo);
        btnSelectPhoto = (Button) findViewById(R.id.btnSelectPhoto);

        add.setOnClickListener(this);
        listButton.setOnClickListener(this);
        btnDatePicker.setOnClickListener(this);
        btnSelectPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d("ok", "ok");
        if (v == btnDatePicker) {
            // Afficher le DatePickerDialog
            showDatePickerDialog();
        } else if (v == btnSelectPhoto) {
            openImageChooser();
        } else if (v == add) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            StringRequest request = new StringRequest(Request.Method.POST,
                    insertUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response);
                    Log.d(TAG, response); // response = "Étudiant ajouté avec succès"
                    //Toast.makeText(AddEtudiant.this, response, Toast.LENGTH_SHORT).show();
                    Toast.makeText(AddEtudiant.this, "Étudiant ajouté avec succès", Toast.LENGTH_SHORT).show();
                    clearFields();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(AddEtudiant.this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    String sexe = "";
                    if (m.isChecked())
                        sexe = "homme";
                    else
                        sexe = "femme";
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("nom", nom.getText().toString());
                    params.put("prenom", prenom.getText().toString());
                    params.put("ville", ville.getSelectedItem().toString());
                    params.put("sexe", sexe);
                    params.put("dateNaissance", selectedDate);
                    params.put("photo", base64Image);
                    return params;
                }
            };
            requestQueue.add(request);
        }else if (v == listButton) {
            // Redirection vers l'activité ListEtudiantActivity
            Intent intent = new Intent(AddEtudiant.this, ListEtudiantActivity.class);
            startActivity(intent);
        }
    }
    // Méthode pour ouvrir le sélecteur d'image
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Sélectionner une image"), PICK_IMAGE_REQUEST);
    }
    // Méthode pour gérer le résultat de la sélection d'image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                photo.setImageBitmap(bitmap);

                // Convertir l'image en Base64
                base64Image = encodeToBase64(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Méthode pour encoder un bitmap en Base64
    private String encodeToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Méthode pour afficher le DatePickerDialog
    private void showDatePickerDialog() {
        // Obtenir la date actuelle
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Créer un DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Mettre à jour le selectedDate quand une date est sélectionnée
                        selectedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                        txtDateNaissance.setText(selectedDate);
                    }
                },
                year, month, day);
        datePickerDialog.show();
    }
    // Méthode pour réinitialiser les champs après ajout
    private void clearFields() {
        nom.setText("");
        prenom.setText("");
        ville.setSelection(0);
        m.setChecked(true);
        selectedDate = "";
        txtDateNaissance.setText("Date non sélectionnée");
        photo.setImageResource(R.drawable.person); // Réinitialiser l'image
        base64Image = ""; // Réinitialiser l'image encodée
    }
}
