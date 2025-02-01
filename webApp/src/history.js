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

function createElementFromHTML(htmlString) {
    var div = document.createElement('div');
    div.innerHTML = htmlString.trim();
    return div.firstElementChild;
}

window.onload = async function () {
    connectionTest()
    let translationHistory = await getTranslationHistory();
    let transcriptionHistory = await getTranscriptionHistory();
    let diarizationHistory = await getDiarizationHistory(); // TODO ZRUPCIE TO ŻEBY DZIAŁAŁO
    const njGlobals = {
        print: console.log,
        Math: Math,
        countries: countries,
        coalesce: coalesce,
        clipString: clipString,
        buildDiarizationText: buildDiarizationText,
        timestampToDate: timestampToDate
    }
    const translationHistoryListView = document.getElementById("translation_history");
    const transcriptionHistoryListView = document.getElementById("transcription_history");
    const translationHistoryViews = [];
    const transcriptionHistoryViews = [];
    if (translationHistory.length > 0) {
        translationHistory.forEach(element => {
            const html = njTemplate.render(Object.assign({}, njGlobals, { event: element, mode: "translation" }));
            translationHistoryViews.push(createElementFromHTML(html));
        });
    } else {
        document.getElementById("translation_history").innerHTML = emptyHistory;
    }
    if (transcriptionHistory.length > 0 || diarizationHistory.length > 0) {
        transcriptionHistory.forEach(element => {
            const html = njTemplate.render(Object.assign({}, njGlobals, { event: element, mode: "transcription" }));
            transcriptionHistoryViews.push(createElementFromHTML(html));
        });
        diarizationHistory.forEach(element => {
            const html = njTemplate.render(Object.assign({}, njGlobals, { event: element, mode: "diarization" }));
            transcriptionHistoryViews.push(createElementFromHTML(html));
        });
    } else {
        transcriptionHistoryListView.innerHTML = emptyHistory;
    }

    let displayedTranslationHistoryViews = transcriptionHistoryViews;
    let displayedTranscriptionHistoryViews = transcriptionHistoryViews;
    let searchedTitle = "";
    let timestampSortingMode = "desc";
    let titleSortingMode = undefined;
    function refreshHistoryLists() {
        translationHistoryListView.innerHTML = "";
        transcriptionHistoryListView.innerHTML = "";
        if (searchedTitle) {
            console.log(`searched title: ${searchedTitle}`);
            const titleFilter = (v) => v.getAttribute("data-title").toLowerCase().includes(searchedTitle.toLowerCase());
            displayedTranslationHistoryViews = translationHistoryViews.filter(titleFilter);
            displayedTranscriptionHistoryViews = transcriptionHistoryViews.filter(titleFilter);
        } else {
            displayedTranslationHistoryViews = translationHistoryViews;
            displayedTranscriptionHistoryViews = transcriptionHistoryViews;
        }
        if (titleSortingMode) {
            console.log(`titleSortingMode: ${titleSortingMode}`);
            const sortKey = (v1, v2) => v1.getAttribute("data-title").localeCompare(v2.getAttribute("data-title")) * (titleSortingMode === "asc" ? 1 : -1);
            displayedTranslationHistoryViews.sort(sortKey);
            displayedTranscriptionHistoryViews.sort(sortKey);
        }
        if (timestampSortingMode) {
            console.log(`timestampSortingMode: ${timestampSortingMode}`);
            const sortKey = (v1, v2) => v1.getAttribute("data-timestamp").localeCompare(v2.getAttribute("data-timestamp")) * (timestampSortingMode === "asc" ? 1 : -1);
            displayedTranslationHistoryViews.sort(sortKey);
            displayedTranscriptionHistoryViews.sort(sortKey);
        }
        displayedTranslationHistoryViews.forEach((v) => translationHistoryListView.appendChild(v));
        displayedTranscriptionHistoryViews.forEach((v) => transcriptionHistoryListView.appendChild(v));
    }
    refreshHistoryLists();

    const filterInput = document.getElementById('filter_input');
    const titleSortButton = document.getElementById("title_sort_button");
    const timestampSortButton = document.getElementById("timestamp_sort_button");
    filterInput.addEventListener("input", (e) => {
        searchedTitle = filterInput.value;
        refreshHistoryLists();
    });
    titleSortButton.addEventListener("click", (e) => {
        titleSortingMode = titleSortingMode !== "desc" ? "desc" : "asc";
        timestampSortingMode = undefined;
        titleSortButton.classList.remove("sorting_undefined");
        titleSortButton.classList.remove("sorting_asc");
        titleSortButton.classList.remove("sorting_desc");
        titleSortButton.classList.add(`sorting_${titleSortingMode}`);
        timestampSortButton.classList.remove("sorting_asc");
        timestampSortButton.classList.remove("sorting_desc");
        timestampSortButton.classList.add("sorting_undefined");
        refreshHistoryLists();
    });
    timestampSortButton.addEventListener("click", (e) => {
        timestampSortingMode = timestampSortingMode !== "desc" ? "desc" : "asc";
        titleSortingMode = undefined;
        timestampSortButton.classList.remove("sorting_undefined");
        timestampSortButton.classList.remove("sorting_asc");
        timestampSortButton.classList.remove("sorting_desc");
        timestampSortButton.classList.add(`sorting_${timestampSortingMode}`);
        titleSortButton.classList.remove("sorting_asc");
        titleSortButton.classList.remove("sorting_desc");
        titleSortButton.classList.add("sorting_undefined");
        refreshHistoryLists();
    });
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
}

async function deleteTranscriptionEvent() {
    let id = 1;
    let result = await deleteTranscription(id);
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
}

async function deleteTranslationEvent() {
    let id = 1;
    let result = await deleteTranslation(id);
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
}

async function deleteDiarizationEvent() {
    let id = 1;
    let result = await deleteDiarization(id);
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
