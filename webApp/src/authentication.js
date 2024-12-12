
const { authenticationClient,
        TranscriptionHistory, 
        TranslationHistory, 
        DiarizationHistory, 
        UserCredits, 
        LoginResponse, 
        Id,
        NewTranscription, 
        NewTranslation, 
        NewDiarization,
        QueryParamethers } = require('./consts.js')

const { getCookie } = require('./token.js')


export function register(username, password) {
    let request = new UserCredits()
    request.setUsername(username)
    request.setPassword(password)
    return new Promise((resolve, reject) => {
        authenticationClient.register(request, {}, (err, response) => {
            if (err) {
                reject(err)
            }
            resolve(response)
        })
    })
}


export function login(username, password) {
    let request = new UserCredits()
    request.setUsername(username)
    request.setPassword(password)
    return new Promise((resolve, reject) => {
        authenticationClient.login(request, {}, (err, response) => {
            if (err) {
                reject(err)
            }
            resolve(response)
        })
    })
}


export async function *getTranscription(start_time, end_time, limit=10) {

    let request = new QueryParamethers()
    // request.setStartTime(start_time)
    // request.setEndTime(end_time)
    request.setLimit(limit)
    let metadata = {}
    let token = getCookie("acs")
    if (token) {
        metadata = {"jwt": token}
    }
    const stream = authenticationClient.getTranscription(request, metadata)
    const responseQueue = []
    let resolveQueue = null
    stream.on('data', (response) => {
        responseQueue.push(response)
        if (resolveQueue) {
            resolveQueue()
            resolveQueue = null
        }
    })
    stream.on('end', () => {
        console.log('Received everything, stream ended.')
        if (resolveQueue) {
            resolveQueue()
            resolveQueue = null
        }
    })
    stream.on('error', (err) => {
        console.error(`There was an error: ${err.code}: ${err.message}`)
        if (resolveQueue) {
            resolveQueue()
            resolveQueue = null
        }
    })

    while (responseQueue.length > 0 || !stream.finished) {
        if (responseQueue.length > 0) {
            yield responseQueue.shift()
        } else {
            await new Promise((resolve) => (resolveQueue = resolve))
        }
    }
}

export function editTranscription(id, newTranscription) {
    let request = new NewTranscription()
    request.setId(id)
    request.setContent(newTranscription)
    let metadata
    let token = getCookie("acs")
    if (token) {
        metadata = {"jwt": token}
    }
    return new Promise((resolve, reject) => {
        authenticationClient.editTranscription(request, metadata, (err, response) => {
            if (err) {
                reject(err)
            }
            resolve(response)
        })
    })
}

export function deleteTranscription(id) {
    let request = new Id()
    request.setId(id)
    let metadata
    let token = getCookie("acs")
    if (token) {
        metadata = {"jwt": token}
    }
    return new Promise((resolve, reject) => {
        authenticationClient.deleteTranscription(request, metadata, (err, response) => {
            if (err) {
                reject(err)
            }
            resolve(response)
        })
    })
}


export function getTranslation() {}
export function editTranslation() {}
export function deleteTranslation() {}

export function getDiarization() {}
export function editDiarization() {}
export function deleteDiarization() {}












// export function *getTranslation(jwtToken) {
//     let request = new Empty()
//     let metadata = {'jwt': jwtToken}
//     console.log("Am in getTranslation")
//     let stream = authenticationClient.getTranslation(request, metadata)

//     yield stream.on('data', (response) => {
//         console.log(`Received response: ${response.getTranscription()}`);
//         return "I received something"
//     });

//     // Handle stream end
//     stream.on('end', () => {
//         console.log('Received everything, stream ended.');
//     });

//     // Handle errors
//     stream.on('error', (err) => {
//         console.log(`There was an error: ${err.code}: ${err.message}`);
//     });
// }