
const { refreshToken } = require('./authentication.js')
const { getCookie } = require('./token.js')
const grpcWeb = require('grpc-web')

export function callHandler(func) {
    return async function (...args) {
        try {
            let response = await func(...args)
            return response
        } catch (err) {
            if (err.code == grpcWeb.UNAUTHENTICATED && err.message == "invalid token") {
                try{
                    console.log("Refreshing token")
                    const refreshToken = getCookie('whisker_refresh')
                    await refreshToken(refreshToken)
                    response = await func(...args)
                    return response
                } catch(e) {
                    throw e
                }
            } 
            throw err
        }
    }
}