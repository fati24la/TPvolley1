<?php
if($_SERVER["REQUEST_METHOD"] == "POST"){
    include_once '../racine.php';
    include_once RACINE.'/service/EtudiantService.php';
    delete();
}

function delete(){
    extract($_POST);
    $es = new EtudiantService();
    $result = $es->delete($es->findById($id));

    // Retourner une réponse (soit un message de succès soit la liste mise à jour)
    header('Content-type: application/json');
    echo json_encode($es->findAllApi());
}
?>