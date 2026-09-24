function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

let movieId = getParameterByName('id');

function handleGenreResult(resultData) {
    let genresElement = jQuery("#genre_list")
    for (let i = 0; i < resultData.length; i++) {
        let genre = resultData[i]["genre"];
        genresElement.append(`<li><a href = "./movie-list.html?genre=${resultData[i]["id"]}">` + genre + "</a></li>");
    }
}

function handleStarsResult(resultData) {
    let starsElement = jQuery("#star_list")
    for (let i = 0; i < resultData.length; i++) {
        let starId = resultData[i]["star_id"];
        let starHTML = "";
        starHTML += `<li><a href="./single-star.html?id=${starId}">` + resultData[i]["star_name"] + "</a></li>";
        starsElement.append(starHTML);
    }
}

function handleResult(resultData) {
    console.log("handleResult: populating movie info from resultData");
    let movieTitleElement = jQuery("#movie_title");
    movieTitleElement.append(resultData[0]["movie_title"])

    let movieYearElement = jQuery("#movie_year")
    movieYearElement.append(resultData[0]["movie_year"])
    let movieDirectorElement = jQuery("#movie_director");
    movieDirectorElement.append("<span>" + resultData[0]["movie_director"] + "</span>");

    let movieRatingElement = jQuery("#movie_rating");
    movieRatingElement.append("<span>" + resultData[0]["movie_rating"] + "</span>");
    createBackHref();
}


function setParamIfNotNull(urlParams, item, itemValue) {
    if (itemValue != null) {
        urlParams.set(item, itemValue);
    }
}

function createBackHref() {
    let anchorBackElement = jQuery("#dynamic-back");
    const movieListParams = new URLSearchParams();
    setParamIfNotNull(movieListParams, "title", sessionStorage.getItem("title"));
    setParamIfNotNull(movieListParams, "star", sessionStorage.getItem("star"));
    setParamIfNotNull(movieListParams, "director", sessionStorage.getItem("director"));
    setParamIfNotNull(movieListParams, "year", sessionStorage.getItem("year"));
    setParamIfNotNull(movieListParams, "N", sessionStorage.getItem("N"));
    setParamIfNotNull(movieListParams, "sort-primary", sessionStorage.getItem("sort-primary"));
    setParamIfNotNull(movieListParams, "sort-secondary", sessionStorage.getItem("sort-secondary"));
    setParamIfNotNull(movieListParams, "offset", sessionStorage.getItem("offset"));
    setParamIfNotNull(movieListParams, "genre", sessionStorage.getItem("genre"));
    setParamIfNotNull(movieListParams, "browse-title", sessionStorage.getItem("browse-title"));
    anchorBackElement.attr("href", "./movie-list.html?" + movieListParams);
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
    url: "api/single-movie?id=" + movieId,
    success: (resultData) => handleResult(resultData)
})

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/stars?id=" + movieId,
    success: (starsData) => handleStarsResult(starsData)
})

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/genres?id=" + movieId,
    success: (resultData) => handleGenreResult(resultData)
});

jQuery("#add_to_cart_button").on("click", function () {
    $.ajax("api/cart", {
        method: "POST",
        data: {item: movieId, action: "add"},
        success: function () {
            alert("Added to cart!");
        },
        error: function () {
            alert("Failed to add to cart.");
        }
    });
});