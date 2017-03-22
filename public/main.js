// function getPhotos(photosData) {
//     for (var i in photosData) {
//         var photo = photosData[i];
//         var elem = $("<img>");
//         elem.attr("src", photo.filename);
//         $("#photos").append(elem);
//     }
// }
// $.get("/photos", getPhotos);

function getPhotos(photosData) {
    for (var i in photosData) {
        // var photo = photosData[i];
        var elem = $("<a>");
        elem.attr("href", "photos/" + photosData[i].filename);
        elem.text(photosData[i].filename);
        $("#photos").append(elem);
        var elem2 = $("<br>");
        $("#photos").append(elem2);
    }
}
$.get("/photos", getPhotos);

function getUser(userData) {
    if(userData.length == 0) {
        $("#login").show();
        $("#upload").hide();

    }
    else {
        $("#logout").show();
        $("#upload").show();
    }
}
$.get("/user", getUser);