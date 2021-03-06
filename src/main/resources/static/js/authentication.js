var sessionToken;

function login() {
    start();

}

function checkLoggedIn() {
    var sessionToken = localStorage.getItem("sessionToken");

    if (!sessionToken) {
        window.location="login.html";
    }

    $.ajax({
        type: "GET",
        url: "/users/user",
        headers: {
            "X-Authorization-Token": sessionToken
        },
        success: checkAuthnResponse,
        error: checkAuthnResponse
    });
}

function checkAuthnResponse(data) {

    if ((!data) || (!data.token)) {
        window.location="login.html";
        return;
    }

    if ((!data.homeLatitude) || (!data.homeLongitude)) {
        window.location="home.html";
        return;
    }

}