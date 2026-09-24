function handleResult(resultData) {
    let genresDivElement = jQuery("#genres-grid");
    console.log(resultData);
    for (let i = 0; i < resultData.length; i++) {
        let genre = resultData[i]["genre"];
        let id = resultData[i]["id"];
        genresDivElement.append(`<a href = "./movie-list.html?genre=${id}">` + genre + `</a>`);
    }

    let alphaNumericDivElement = jQuery("#movie-title-alphabet")

    for (let i = 0; i < 10; i++) {
        let title = i;
        alphaNumericDivElement.append(`<a href = "./movie-list.html?browse-title=${title}">` + title + `</a>`);
    }

    for (let i = 65; i < 91; i++) {
        let title = String.fromCharCode(i);
        alphaNumericDivElement.append(`<a href = "./movie-list.html?browse-title=${title}">` + title + `</a>`);
    }

    alphaNumericDivElement.append(`<a href = "./movie-list.html?browse-title=*">*</a>`)
}

let searchForm = $("#input-div");

function submitSearch(formSubmissionEvent) {
    console.log("Submitting Search...");
    formSubmissionEvent.preventDefault();
    const params = new URLSearchParams(searchForm.serialize());
    window.location.assign("./movie-list.html?" + params);
}

searchForm.submit(submitSearch);

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/genres",
    success: (resultData) => handleResult(resultData)
});