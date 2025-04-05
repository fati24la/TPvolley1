<?php
class Etudiant {
    private $id;
    private $nom;
    private $prenom;
    private $ville;
    private $sexe;
    private $dateNaissance;
    private $photo;

    function __construct($id, $nom, $prenom, $ville, $sexe,$dateNaissance, $photo) {
        $this->id = $id;
        $this->nom = $nom;
        $this->prenom = $prenom;
        $this->ville = $ville;
        $this->sexe = $sexe;
        $this->dateNaissance = $dateNaissance;
        $this->photo = $photo;
    }

    function getId() {
        return $this->id;
    }
    function getNom() {
        return $this->nom;
    }
    function getPrenom() {
        return $this->prenom;
    }
    function getVille() {
        return $this->ville;
    }

    function getSexe() {
        return $this->sexe;
    }

    function setId($id) {
        $this->id = $id;
    }

    function setNom($nom) {
        $this->nom = $nom;
    }

    function setPrenom($prenom) {
        $this->prenom = $prenom;
    }

    function setVille($ville) {
        $this->ville = $ville;
    }
    function setSexe($sexe) {
        $this->sexe = $sexe;
    }
    function getDateNaissance() {
        return $this->dateNaissance;
    }

    function setDateNaissance($dateNaissance) {
        $this->dateNaissance = $dateNaissance;
    }

    function getPhoto() {
        return $this->photo;
    }

    function setPhoto($photo) {
        $this->photo = $photo;
    }
    public function __toString() {
        return $this->nom . " " . $this->prenom;
    }



}