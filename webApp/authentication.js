
const { authenticationClient,
        TextHistory, 
        UserCredits, 
        StatusResponse, 
        LoginResponse, 
        Empty} = require('./consts.js')

export function button_register(e)
{
    e.preventDefault()
    register("Szapka", "Szapka")
}

export function button_login(e)
{
    e.preventDefault()
    let token = login("Szapka", "Szapka")
}

export function button_getTranslation(e)
{
    e.preventDefault()
    const stream = getTranslation("Token here")
    for (const res of stream) {
        console.log(res)
    }
}


export function register(username, password) {
    let request = new UserCredits()
    request.setUsername(username)
    request.setPassword(password)
    authenticationClient.register(request, {}, (err, response) => {
        if (err) {
            console.log(`There was an error when executing register function: ${err.code}, message = ${err.message}`)
            return
        }
        let success = response.getSuccessful()
        console.log(response)
        if (!success) {
            console.log(`Some error when registering new user on server: ${response.getError()}`)
            return
        }
        console.log(`Success when registering new user!`)
    })
}

function login(username, password) {
    let request = new UserCredits()
    request.setUsername(username)
    request.setPassword(password)
    authenticationClient.login(request, {}, (err, response) => {
        if (err) {
            console.log(`There was an error when executing login function: ${err.code}, message = ${err.message}`)
            return
        }
        let success = response.getSuccessful()
        if (!success) {
            console.log('Wrong username or password.')
            return
        } 
        let token = response.getJwt()
        console.log("Logged in!")
        console.log(token)
        return token
    })
}


function *getTranslation(jwtToken) {
    console.log("AAAA")
    let request = new Empty()
    let metadata = {'jwt': jwtToken}
    console.log("Am in getTranslation")
    let stream = authenticationClient.getTranslation(request, metadata)

    yield stream.on('data', (response) => {
        console.log(`Received response: ${response.getTranscription()}`);
        return "I received something"
    });

    // Handle stream end
    stream.on('end', () => {
        console.log('Received everything, stream ended.');
    });

    // Handle errors
    stream.on('error', (err) => {
        console.log(`There was an error: ${err.code}: ${err.message}`);
    });
}