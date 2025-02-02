
const { authenticationClient,
        UserCredits, 
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
    console.log(metadata)
    const stream = authenticationClient.getTranscription(request, metadata)
    const responseQueue = []
    let resolveQueue = null;
    let streamEnded = false;

    stream.on('data', (response) => {
        responseQueue.push(response);
        if (resolveQueue) {
            resolveQueue();
            resolveQueue = null;
        }
    });

    stream.on('end', () => {
        console.log('Received everything, stream ended.');
        streamEnded = true;
        if (resolveQueue) {
            resolveQueue();
            resolveQueue = null;
        }
    });

    stream.on('error', (err) => {
        console.error(`There was an error: ${err.code}: ${err.message}`);
        streamEnded = true;
        if (resolveQueue) {
            resolveQueue();
            resolveQueue = null;
        }
    });

    while (responseQueue.length > 0 || !streamEnded) {
        if (responseQueue.length > 0) {
            yield responseQueue.shift();
        } else {
            await new Promise((resolve) => (resolveQueue = resolve));
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


export async function *getTranslation(start_time, end_time, limit) {
    let request = new QueryParamethers()
    // request.setStartTime(start_time)
    // request.setEndTime(end_time)
    request.setLimit(limit)
    let metadata = {}
    let token = getCookie("acs")
    if (token) {
        metadata = {"jwt": token}
    }
    const stream = authenticationClient.getTranslation(request, metadata)
    const responseQueue = []
    let resolveQueue = null
    let streamEnded = false

    stream.on('data', (response) => {
        responseQueue.push(response)
        if (resolveQueue) {
            resolveQueue()
            resolveQueue = null
        }
    })
    stream.on('end', () => {
        console.log('Received everything, stream ended.')
        streamEnded = true
        if (resolveQueue) {
            resolveQueue()
            resolveQueue = null
        }
    })
    stream.on('error', (err) => {
        console.error(`There was an error: ${err.code}: ${err.message}`)
        streamEnded = true
        if (resolveQueue) {
            resolveQueue()
            resolveQueue = null
        }
    })

    while (responseQueue.length > 0 || !streamEnded) {
        if (responseQueue.length > 0) {
            yield responseQueue.shift()
        } else {
            await new Promise((resolve) => (resolveQueue = resolve))
        }
    }
}


export function editTranslation(id, transcription, translation, edit_transcription, edit_translation) {
    let request = new NewTranslation()
    request.setId(id)
    request.setTranscription(transcription)
    request.setTranslation(translation)
    request.setEditTranscription(edit_transcription)
    request.setEditTranslation(edit_translation)
    let metadata
    let token = getCookie("acs")
    if (token) {
        metadata = {"jwt": token}
    }
    return new Promise((resolve, reject) => {
        authenticationClient.editTranslation(request, metadata, (err, response) => {
            if (err) {
                reject(err)
            }
            resolve(response)
        })
    })
}


export function deleteTranslation(id) {
    let request = new Id()
    request.setId(id)
    let metadata
    let token = getCookie("acs")
    if (token) {
        metadata = {"jwt": token}
    }
    return new Promise((resolve, reject) => {
        authenticationClient.deleteTranslation(request, metadata, (err, response) => {
            if (err) {
                reject(err)
            }
            resolve(response)
        })
    })
}


export async function *getDiarization(start_time, end_time, limit) {
    let request = new QueryParamethers()
    // request.setStartTime(start_time)
    // request.setEndTime(end_time)
    request.setLimit(limit)
    let metadata = {}
    let token = getCookie("acs")
    if (token) {
        metadata = {"jwt": token}
    }
    const stream = authenticationClient.getDiarization(request, metadata)
    const responseQueue = []
    let resolveQueue = null
    let streamEnded = false

    stream.on('data', (response) => {
        responseQueue.push(response)
        if (resolveQueue) {
            resolveQueue()
            resolveQueue = null
        }
    })
    stream.on('end', () => {
        console.log('Received everything, stream ended.')
        streamEnded = true
        if (resolveQueue) {
            resolveQueue()
            resolveQueue = null
        }
    })
    stream.on('error', (err) => {
        console.error(`There was an error: ${err.code}: ${err.message}`)
        streamEnded = true
        if (resolveQueue) {
            resolveQueue()
            resolveQueue = null
        }
    })

    while (responseQueue.length > 0 || !streamEnded) {
        if (responseQueue.length > 0) {
            yield responseQueue.shift()
        } else {
            await new Promise((resolve) => (resolveQueue = resolve))
        }
    }
}


export function editDiarization(id, line, speaker) {
    let request = new NewDiarization()
    request.setId(id)
    request.setLine(line)
    request.setSpeaker(speaker)
    let metadata
    let token = getCookie("acs")
    if (token) {
        metadata = {"jwt": token}
    }
    return new Promise((resolve, reject) => {
        authenticationClient.editDiarization(request, metadata, (err, response) => {
            if (err) {
                reject(err)
            }
            resolve(response)
        })
    })
}


export function deleteDiarization(id) {
    let request = new Id()
    request.setId(id)
    let metadata
    let token = getCookie("acs")
    if (token) {
        metadata = {"jwt": token}
    }
    return new Promise((resolve, reject) => {
        authenticationClient.deleteDiarization(request, metadata, (err, response) => {
            if (err) {
                reject(err)
            }
            resolve(response)
        })
    })
}