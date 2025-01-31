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

const countryNames = {
    "Polish": "pl",
    "en": "gb",
};

function getCountry(langCode) {
    let countryCode = langCode;
    if (countryNames[langCode] !== undefined) countryCode = countryNames[langCode];
    if (langCode.includes('-')) countryCode = getCountry(langCode.split('-')[1]);
    return countryCode.toLowerCase();
}

window.onload = async function () {
    connectionTest()
    let translationHistory = await getTranslationHistory();
    let transcriptionHistory = await getTranscriptionHistory();
    let diarizationHistory = await getDiarizationHistory(); // TODO ZRUPCIE TO ŻEBY DZIAŁAŁO
    console.log(translationHistory);
    console.log(transcriptionHistory);
    console.log(diarizationHistory);
    const njGlobals = {
        getCountry: getCountry,
        clipString: clipString,
        buildDiarizationText: buildDiarizationText,
        timestampToDate: timestampToDate
    }
    if (translationHistory.length > 0) {
        translationHistory.forEach(element => {
            const html = njTemplate.render(Object.assign({}, njGlobals, { event: element, mode: "translation" }));
            document.getElementById("translation_history").innerHTML += html;
        });
    } else {
        document.getElementById("translation_history").innerHTML = emptyHistory;
    }
    if (transcriptionHistory.length > 0 || diarizationHistory.length > 0) {
        transcriptionHistory.forEach(element => {
            const html = njTemplate.render(Object.assign({}, njGlobals, { event: element, mode: "transcription" }));
            document.getElementById("transcription_history").innerHTML += html;
        });
        diarizationHistory.forEach(element => {
            const html = njTemplate.render(Object.assign({}, njGlobals, { event: element, mode: "diarization" }));
            document.getElementById("transcription_history").innerHTML += html;
        });
    } else {
        document.getElementById("transcription_history").innerHTML = emptyHistory;
    }
}

async function getTranscriptionHistory() {
    const start_time = new proto.google.protobuf.Timestamp()
    start_time.fromDate(new Date("2024-01-01T00:00:00Z"))
    let end_time = new proto.google.protobuf.Timestamp()
    end_time.fromDate(new Date())
    let limit = 10
    let transcriptions = getTranscription(start_time, end_time, limit)
    const responseList = []
    for await (const transcription of transcriptions) {
        responseList.push(transcription);
    }
    return new Promise((resolve) => { resolve(responseList) });
}

async function editTranscriptionEvent() {
    let id = 1;
    let newTranscription = "Here is a text of an edited transcription";
    let result = await editTranscription(id, newTranscription);
    console.log(result);
}

async function deleteTranscriptionEvent() {
    let id = 1;
    let result = await deleteTranscription(id);
    console.log(result);
}

async function getTranslationHistory() {
    const start_time = new proto.google.protobuf.Timestamp();
    start_time.fromDate(new Date("2024-01-01T00:00:00Z"));
    let end_time = new proto.google.protobuf.Timestamp();
    end_time.fromDate(new Date());
    let limit = 10;
    let translations = await getTranslation(start_time, end_time, limit);
    const responseList = [];
    for await (const translation of translations) {
        responseList.push(translation);
    }
    return new Promise((resolve) => resolve(responseList));
}

async function editTranslationEvent() {
    let id = 1;
    let transcription = "Here is a text of an edited transcription";
    let translation = "Here is a text of an edited translation";
    let edit_transcription = true;
    let edit_translation = true;
    let result = await editTranslation(id, transcription, translation, edit_transcription, edit_translation);
    console.log(result);
}

async function deleteTranslationEvent() {
    let id = 1;
    let result = await deleteTranslation(id);
    console.log(result);
}

async function getDiarizationHistory() {
    const start_time = new proto.google.protobuf.Timestamp();
    start_time.fromDate(new Date("2024-01-01T00:00:00Z"));
    let end_time = new proto.google.protobuf.Timestamp();
    end_time.fromDate(new Date());
    let limit = 2;
    let dialogs = await getDiarization(start_time, end_time, limit);
    const responseList = []
    for await (const dialog of dialogs) {
        responseList.push(dialog)
    }
    return new Promise((resolve) => resolve(responseList));
}

async function editDiarizationEvent() {
    let id = 1;
    let line = ["AAAAA", "BBBBB"];
    let speaker = ["SPEK1", "SPOK2"];
    let result = await editDiarization(id, line, speaker);
    console.log(result);
}

async function deleteDiarizationEvent() {
    let id = 1;
    let result = await deleteDiarization(id);
    console.log(result);
}

function timestampToDate(timestamp) {
    const milliseconds = timestamp.getSeconds() * 1000 + timestamp.getNanos() / 1e6;
    const resultDate = new Date(milliseconds);

    const date = resultDate.toISOString().slice(0, 10)
    const time = resultDate.toTimeString().slice(0, 8)

    return `${date} ${time}`
}

function createElement(type, id, classes) {
    const element = document.createElement(type);
    if (id) element.id = id;
    if (classes) element.classList.add(...classes);
    return element;
}

function openTab(tile, tab) {
    document.querySelectorAll('.tab_button').forEach(e => e.classList.remove('selected'));
    tile.classList.add('selected');
    document.querySelectorAll('.tab').forEach((t) => t.classList.add('hidden'));
    document.getElementById(`${tab}_history`).classList.remove('hidden');
}

function speak(text, language) {
    const message = new SpeechSynthesisUtterance();
    message.text = text;
    message.lang = language;
    const speechSynthesis = window.speechSynthesis;
    speechSynthesis.speak(message);
}

window.openTab = openTab
