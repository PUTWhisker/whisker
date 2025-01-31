
const { register, login } = require('./authentication.js')
const { connectionTest } = require('./send-file.js')

window.onload = function () {
    connectionTest();
    loginButton = document.getElementById("login");
    signupButton = document.getElementById("register");
    if (loginButton) {
        loginButton.addEventListener('click', retrieveToken);
    } else if (signupButton) {
        signupButton.addEventListener('click', registerUser);
    }
}


async function registerUser() {
    try {
        username = document.getElementById("username").value;
        password = document.getElementById("password").value;
        validateCredentials(username, password);
        let response = await register(username, password);
        window.location.href = `/webApp/${localStorage.getItem('whisker-last-page')}`;
    } catch (err) {
        alert(err.message);
    }
}


async function retrieveToken() {
    try {
        username = document.getElementById("username").value;
        password = document.getElementById("password").value;
        validateCredentials(username, password);
        response = await login(username, password);
        token = response.getJwt();
        document.cookie = `acs=${token}; SameSite=Strict; `;
        window.location.href = `/webApp/${localStorage.getItem('whisker-last-page')}`;
    } catch (err) {
        alert(err.message);
    }
}


function validateCredentials(username, password) {
    if (username.length < 3)
        throw Error("Username needs to contain at least 3 characters");
    if (password.length < 3)
        throw Error("Password needs to contain at least 3 characters");
}