let add_movie_form = $("#movie_insert");
let add_star_form = $("#star_insert");

function addFormMovie(formSubmitEvent) {
    console.log("add movie form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "../api/dashboard", {
            method: "POST",
            data: add_movie_form.serialize(),
            success: resultDataString => {
                alert(resultDataString["message"]);
            },
        }
    );
}

function addFormStar(formSubmitEvent) {
    console.log("add star form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "../api/dashboard", {
            method: "POST",
            data: add_star_form.serialize(),
            success: resultDataString => {
                if (resultDataString["status"] === "success") {
                    alert("Added star " + resultDataString["id"]);
                }
                else {
                    alert(resultDataString["message"]);
                }
            },
        }
    );
}


add_movie_form.submit(addFormMovie);
add_star_form.submit(addFormStar);