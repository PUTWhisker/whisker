
const { register, login } = require('./authentication.js')
const { connectionTest } = require('./send-file.js')

window.onload = function() {
    connectionTest()
    loginButton = document.getElementById("login")
    signupButton = document.getElementById("register")
    if (loginButton) {
        loginButton.addEventListener('click', retrieveToken)
    } else if (signupButton) {
        signupButton.addEventListener('click', registerUser)
    }
}


async function registerUser() {
    try{
        username = document.getElementById("username").value
        password = document.getElementById("password").value
        validateCredentials(username, password)
        let response = await register(username, password)
        console.log(response)
        console.log("Registeration completed!")
    } catch (err) {
        console.log(`An error occured: ${err.code}, ${err.message}`)
    }
}


async function retrieveToken() {
    try{
        username = document.getElementById("username").value
        password = document.getElementById("password").value
        validateCredentials(username, password)
        response = await login(username, password)
        console.log(response)
        success = response.getSuccessful()
        if (!success) {
            throw Error("Couldn't login.")
        }
        token = response.getJwt()
        document.cookie = `acs=${token}; SameSite=Strict; `
        console.log(token)
    } catch (err) {
        console.log(`An error occured: ${err.code}, ${err.message}`)
    }
}


function validateCredentials(username, password) {
    if (username.length < 3)
        throw Error("Username needs to contain at least 3 characters")
    if (password.length < 3)
        throw Error("Password needs to contain at least 3 characters")
}