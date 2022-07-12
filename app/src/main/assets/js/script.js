function openReadAlong() {
    androidInteract.triggerReadAlong("hp_g3_hinP4")
}

function onReadAlongResult(correctWords, timeTaken){
    document.getElementById("results").innerHTML = "Read Along : Correct words : " + correctWords + " : Time taken : " + timeTaken + " seconds";
}