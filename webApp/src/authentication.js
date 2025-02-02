
const { authenticationClient,
        UserCredits, 
        Id,
        NewTranscription, 
        NewTranslation, 
        NewDiarization,
        QueryParamethers,
        RefreshTokenRequest } = require('./consts.js')

const { callHandler } = require('./interceptor.js')
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


export async function refreshToken() {
    let request = new RefreshTokenRequest()
    let token = getCookie("whisker_refresh")
    request.setRefreshToken(token)
    return new Promise((resolve, reject) => {
        authenticationClient.refreshToken(request, {}, (err, response) => {
            if (err) {
                reject(err)
            }
            let newAccessToken = response.getAccessToken();
            let newRefreshToken = response.getRefreshToken();
            document.cookie = `whisker_access=${newAccessToken}; SameSite=Strict; `;
            document.cookie = `whisker_refresh=${newRefreshToken}; SameSite=Strict; `;
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
    let token = getCookie("whisker_access")
    if (token) {
        metadata = {"jwt": token}
    }
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
    function _editTranscription(id, newTranscription) {
        return new Promise((resolve, reject) => {
            let request = new NewTranscription()
            request.setId(id)
            request.setContent(newTranscription)
            let metadata
            let token = getCookie("whisker_access")
            if (token) {
                metadata = {"jwt": token}
            }
            authenticationClient.editTranscription(request, metadata, (err, response) => {
                if (err) {
                    reject(err)
                }
                resolve(response)
            })
        })
    }
    return new Promise((resolve, reject) => {
        resolve(callHandler(_editTranscription)(id, newTranscription))
    })

}


export function deleteTranscription(id) {
    function _deleteTranscription(id) {
        return new Promise((resolve, reject) => {
            let request = new Id()
            request.setId(id)
            let metadata
            let token = getCookie("whisker_access")
            if (token) {
                metadata = {"jwt": token}
            }
            authenticationClient.deleteTranscription(request, metadata, (err, response) => {
                if (err) {
                    reject(err)
                }
                resolve(response)
            })
        })
    }
    return new Promise((resolve, reject) => {
        resolve(callHandler(_deleteTranscription)(id))
    })

}


export async function *getTranslation(start_time, end_time, limit) {
    let request = new QueryParamethers()
    // request.setStartTime(start_time)
    // request.setEndTime(end_time)
    request.setLimit(limit)
    let metadata = {}
    let token = getCookie("whisker_access")
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
    function _editTranslation(id, transcription, translation, edit_transcription, edit_translation) {
        return new Promise((resolve, reject) => {
            let request = new NewTranslation()
            request.setId(id)
            request.setTranscription(transcription)
            request.setTranslation(translation)
            request.setEditTranscription(edit_transcription)
            request.setEditTranslation(edit_translation)
            let metadata
            let token = getCookie("whisker_access")
            if (token) {
                metadata = {"jwt": token}
            }
            authenticationClient.editTranslation(request, metadata, (err, response) => {
                if (err) {
                    reject(err)
                }
                resolve(response)
            })
        })
    }
    return new Promise((resolve, reject) => {
        resolve(callHandler(_editTranslation)(id, transcription, translation, edit_transcription, edit_translation))
    })

}


export function deleteTranslation(id) {
    function _deleteTranslation(id,) {
        return new Promise((resolve, reject) => {
            let request = new Id()
            request.setId(id)
            let metadata
            let token = getCookie("whisker_access")
            if (token) {
                metadata = {"jwt": token}
            }
            authenticationClient.deleteTranslation(request, metadata, (err, response) => {
                if (err) {
                    reject(err)
                }
                resolve(response)
            })
        })
    }
    return new Promise((resolve, reject) => {
        resolve(callHandler(_deleteTranslation)(id))
    })

}


export async function *getDiarization(start_time, end_time, limit) {
    let request = new QueryParamethers()
    // request.setStartTime(start_time)
    // request.setEndTime(end_time)
    request.setLimit(limit)
    let metadata = {}
    let token = getCookie("whisker_access")
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
    function _editDiarization(id, line, speaker) {
        return new Promise((resolve, reject) => {
            let request = new NewDiarization()
            request.setId(id)
            request.setLine(line)
            request.setSpeaker(speaker)
            let metadata
            let token = getCookie("whisker_access")
            if (token) {
                metadata = {"jwt": token}
            }
            authenticationClient.editDiarization(request, metadata, (err, response) => {
                if (err) {
                    reject(err)
                }
                resolve(response)
            })
        })
    }
    return new Promise((resolve, reject) => {
        resolve(callHandler(_editDiarization)(id, line, speaker))
    })

}


export function deleteDiarization(id) {
    function _deleteDiarization(id,) {
        return new Promise((resolve, reject) => {
            let request = new Id()
            request.setId(id)
            let metadata
            let token = getCookie("whisker_access")
            if (token) {
                metadata = {"jwt": token}
            }
            authenticationClient.deleteDiarization(request, metadata, (err, response) => {
                if (err) {
                    reject(err)
                }
                resolve(response)
            })
        })
    }
    return new Promise((resolve, reject) => {
        resolve(callHandler(_deleteDiarization)(id))
    })

}