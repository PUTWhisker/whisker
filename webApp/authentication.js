import { Client } from '@grpc/grpc-js'

const { ClientServiceClient,
        TextHistory, 
        UserCredits, 
        StatusResponse, 
        LoginResponse, 
        Empty} = require('./consts.js')


export function register(username, password) {
    let registerRequest = new UserCredits()
    registerRequest.setUsername(username)
    registerRequest.setPassword(password)
    ClientServiceClient.register(registerRequest, {}, (err, response) => {
        if (err) {
            console.log(`There was an error when executing register function: ${err.code}, message = ${err.message}`)
            return
        }
        let success = response.getSuccessful()
        if (!success) {
            console.log(`Some error when registering new user on server: ${response.getError()}`)
            return
        }
        console.log(`Success when registering new user!`)
    })
}

export function login(username, password) {
    let loginRequest = new UserCredits()
    loginRequest.setUsername(username)
    loginRequest.setPassword(password)
    ClientServiceClient.login(loginRequest, {}, (err, response) => {
        if (err) {
            console.log(`There was an error when executing login function: ${err.code}, message = ${err.message}`)
            return
        }
        let success = response.getSuccessful()
        if (!success) {
            console.log('Wrong username or password.')
            return
        } 
        let token = response.getJWT()
        console.log("Logged in!")
        return token
    })
}


function getTranslation(jwtToken) {
    let getTranslationRequest = new Empty()
    let meta = new grpc.Metadata()
    meta.add('jwt', jwtToken)
    ClientServiceClient.getTranslation(getTranslationRequest, meta, (err, response) => {
        if (err) {
            console.log(`There was an error when executing getTranslation function: ${err.code}, message = ${err.message}`)
            return
        }
        //TODO: Tu stream trzeba obsłużyć i sprawdzić działanie metadaty
    })
}