const { getTranscription,
        editTranscription,
        deleteTranscription,
        getTranslation,
        editTranslation,
        deleteTranslation,
        getDiarization,
        editDiarization,
        deleteDiarization } = require('./authentication.js')

const { connectionTest } = require('./send-file.js')

window.onload = function() {
    connectionTest()
    let getTranscriptionButton = document.getElementById("getTranscription")
    let editTranscriptionButton = document.getElementById("editTranscription")
    let deleteTranscriptionButton = document.getElementById("deleteTranscription")
    
    let getTranslationButton = document.getElementById("getTranslation")
    let editTranslationButton = document.getElementById("editTranslation")
    let deleteTranslationButton = document.getElementById("deleteTranslation")
    
    let getDiarizationButton = document.getElementById("getDiarization")
    let editDiarizationButton = document.getElementById("editDiarization")
    let deleteDiarizationButton = document.getElementById("deleteDiarization")

    getTranscriptionButton.addEventListener("click", getTranscriptionEvent)
    editTranscriptionButton.addEventListener("click", editTranscriptionEvent)
    deleteTranscriptionButton.addEventListener("click", deleteTranscriptionEvent)

    getTranslationButton.addEventListener("click", getTranslationEvent)
    editTranslationButton.addEventListener("click", editTranslationEvent)
    deleteTranslationButton.addEventListener("click", deleteTranslationEvent)
    
    getDiarizationButton.addEventListener("click", getDiarizationEvent)
    editDiarizationButton.addEventListener("click", editDiarizationEvent)
    deleteDiarizationButton.addEventListener("click", deleteDiarizationEvent)
}


async function getTranscriptionEvent() {
    const start_time = new proto.google.protobuf.Timestamp()
    start_time.fromDate(new Date("2024-01-01T00:00:00Z"))
    let end_time = new proto.google.protobuf.Timestamp()
    end_time.fromDate(new Date())
    let limit = 2
    let transcriptions = getTranscription(start_time, end_time, limit)
    for await (const transcription of transcriptions) {
        console.log(transcription.getTranscription())
    }
}


async function editTranscriptionEvent() {
    let id = 1
    let newTranscription = "Here is a text of an edited transcription"
    let result = await editTranscription(id, newTranscription)
    console.log(result)
}


async function deleteTranscriptionEvent() {
    let id = 1
    let result = await deleteTranscription(id)
    console.log(result)
}


async function getTranslationEvent() {
    const start_time = new proto.google.protobuf.Timestamp()
    start_time.fromDate(new Date("2024-01-01T00:00:00Z"))
    let end_time = new proto.google.protobuf.Timestamp()
    end_time.fromDate(new Date())
    let limit = 5
    let translations = await getTranslation(start_time, end_time, limit)
    for await (const translation of translations) {
        console.log(translation.getTranslation())
        console.log(translation.getTranscription())
    }
}


async function editTranslationEvent() {
    let id = 1
    let transcription = "Here is a text of an edited transcription"
    let translation = "Here is a text of an edited translation"
    let edit_transcription = true
    let edit_translation = true
    let result = await editTranslation(id, transcription, translation, edit_transcription, edit_translation)
    console.log(result)
}


async function deleteTranslationEvent() {
    let id = 1
    let result = await deleteTranslation(id)
    console.log(result)
}


async function getDiarizationEvent() {
    const start_time = new proto.google.protobuf.Timestamp()
    start_time.fromDate(new Date("2024-01-01T00:00:00Z"))
    let end_time = new proto.google.protobuf.Timestamp()
    end_time.fromDate(new Date())
    let limit = 2
    let dialogs = await getDiarization(start_time, end_time, limit)
    for await (const dialog of dialogs) {
        console.log(dialog.getSpeaker() + "  " + dialog.getLine())
    }
}


async function editDiarizationEvent() {
    let id = 1
    let line = ["AAAAA", "BBBBB"]
    let speaker = ["SPEK1", "SPOK2"]
    let result = await editDiarization(id, line, speaker)
    console.log(result)
}


async function deleteDiarizationEvent() {
    let id = 1
    let result = await deleteDiarization(id)
    console.log(result)
}

