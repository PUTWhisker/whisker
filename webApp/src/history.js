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

const flags = {
    'pl': '🇵🇱',
    'en': '🇬🇧',
    'es-ES': '🇪🇸'
};

window.onload = async function () {
    connectionTest()
    let transcriptionHistory = await getTranscriptionHistory()
    let translationHistory = await getTranslationHistory()
    transcriptionHistory.forEach(element => {
        document.getElementById("transcription_history").appendChild(createView(element, 'transcription'));
        // const html = njTemplate.render({ event: element, flags: flags, clipString: clipString });
        // document.getElementById("transcription_history").innerHTML += html;
    });
    translationHistory.forEach(element => {
        document.getElementById("translation_history").appendChild(createView(element, 'translation'));
        // const html = njTemplate.render({ event: element, flags: flags, clipString: clipString });
        // document.getElementById("transcription_history").innerHTML += html;
    });
    // let getTranscriptionButton = document.getElementById("getTranscription")
    // let editTranscriptionButton = document.getElementById("editTranscription")
    // let deleteTranscriptionButton = document.getElementById("deleteTranscription")

    // let getTranslationButton = document.getElementById("getTranslation")
    // let editTranslationButton = document.getElementById("editTranslation")
    // let deleteTranslationButton = document.getElementById("deleteTranslation")

    // let getDiarizationButton = document.getElementById("getDiarization")
    // let editDiarizationButton = document.getElementById("editDiarization")
    // let deleteDiarizationButton = document.getElementById("deleteDiarization")

    // getTranscriptionButton.addEventListener("click", getTranscriptionHistory)
    // editTranscriptionButton.addEventListener("click", editTranscriptionEvent)
    // deleteTranscriptionButton.addEventListener("click", deleteTranscriptionEvent)

    // getTranslationButton.addEventListener("click", getTranslationHistory)
    // editTranslationButton.addEventListener("click", editTranslationEvent)
    // deleteTranslationButton.addEventListener("click", deleteTranslationEvent)

    // getDiarizationButton.addEventListener("click", getDiarizationHistory)
    // editDiarizationButton.addEventListener("click", editDiarizationEvent)
    // deleteDiarizationButton.addEventListener("click", deleteDiarizationEvent)
}

// document.addEventListener('DOMContentLoaded', (e) => {

//     getTranslationHistory().then((historyJSON) => {
//         const history = JSON.parse(historyJSON).history;
//         history.forEach(element => {
//             const html = njTemplate.render({ event: element, flags: flags, clipString: clipString });
//             document.getElementById("transcription_history").innerHTML += html;
//         });
//     });
//     getTranscribeHistory().then((historyJSON) => {
//         const history = JSON.parse(historyJSON).history;
//         history.forEach(element => {
//             const html = njTemplate.render({ event: element, flags: flags, clipString: clipString });
//             document.getElementById("translation_history").innerHTML += html;
//         });
//     });
//     getDiarizationHistory().then((historyJSON) => {
//         const history = JSON.parse(historyJSON).history;
//         history.forEach(element => {
//             const html = njTemplate.render({ event: element, flags: flags, clipString: clipString });
//             document.getElementById("diarization_history").innerHTML += html;
//         });
//     });

// });


async function getTranscriptionHistory() {
    const start_time = new proto.google.protobuf.Timestamp()
    start_time.fromDate(new Date("2024-01-01T00:00:00Z"))
    let end_time = new proto.google.protobuf.Timestamp()
    end_time.fromDate(new Date())
    let limit = 10
    let transcriptions = getTranscription(start_time, end_time, limit)
    const responseList = []
    for await (const transcription of transcriptions) {
        console.log(transcription.getTranscription())
        responseList.push(transcription)
    }
    console.log("I co")
    return new Promise((resolve) => {resolve(responseList)})
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
    const responseList = []
    for await (const translation of translations) {
        responseList.push(translation)
    }
    return new Promise((resolve) => resolve(responseList))
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
    for await (const dialog of dialogs) {
        console.log(dialog.getSpeaker() + "  " + dialog.getLine());
    }
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

function spook(event, translation) {
    const message = new SpeechSynthesisUtterance();
    message.text = !translation ? event.transcription_result : event.translation_result;
    message.lang = !translation ? event.selected_options.original_language : event.selected_options.translate_language;
    const speechSynthesis = window.speechSynthesis;
    speechSynthesis.speak(message);
}

function clipString(str) {
    const limit = 128;
    if (str.length > limit) return `${str.substring(0, limit - 3)}...`
    else return str;
}

function createView(event, mode) {
    const view = createElement('div', null, ['event_view']);

    const dateView = createElement('div', null, ['event_date_view']);
    view.appendChild(dateView);
    dateView.innerText = timestampToDate(event.getCreatedAt())

    const titleView = createElement('div', null, ['event_title']);
    view.appendChild(titleView);
    //titleView.innerText = event.title;
    titleView.innerText = "Titles not fully supported yet (I guess)"

    const previewsView = createElement('div', null, ['event_previews_view']);
    view.appendChild(previewsView);
    {
        const transcriptionView = createElement('div', null, ['event_text_row']);
        previewsView.appendChild(transcriptionView);
        {
            const transcriptionflag = createElement('div', null, ['text_flag']);
            transcriptionView.appendChild(transcriptionflag);
            //transcriptionflag.innerText = flags[event.selected_options.original_language];
            if (mode == 'translation') {
                transcriptionflag.innerText = flags[event.getTranscriptionLangauge()]
            } else {
                transcriptionflag.innerText = flags[event.getLanguage()]
            }

            const transcriptionViewText = createElement('div', null, ['preview_text']);
            transcriptionView.appendChild(transcriptionViewText);
            transcriptionViewText.innerText = clipString(event.getTranscription());

            const audioIcon = createElement('button', null, ['option_button']);
            transcriptionView.appendChild(audioIcon);
            audioIcon.innerHTML = '<span class="material-symbols-outlined">brand_awareness</span>';
            audioIcon.onclick = () => spook(event, false);

            const transcriptionCopyButton = createElement('button', null, ['option_button']);
            transcriptionView.appendChild(transcriptionCopyButton);
            transcriptionCopyButton.innerHTML = '<span class="material-symbols-outlined">content_copy</span>';
            transcriptionCopyButton.onclick = (_) => copyToClipboard(event.getTranscription());

            const transcriptionDownloadButton = createElement('button', null, ['option_button']);
            transcriptionView.appendChild(transcriptionDownloadButton);
            transcriptionDownloadButton.innerHTML = '<span class="material-symbols-outlined">download</span>';
            transcriptionDownloadButton.onclick = (_) => downloadAsFile(event.getTranscription(), 'transcription.txt');
        }

        if (mode == 'translation') {
            const translationView = createElement('div', null, ['event_text_row']);
            previewsView.appendChild(translationView);
            {
                const translationflag = createElement('div', null, ['text_flag']);
                translationView.appendChild(translationflag);
                translationflag.innerText = flags[event.getTranslationLangauge()];

                const translationViewText = createElement('div', null, ['preview_text']);
                translationView.appendChild(translationViewText);
                translationViewText.innerText = clipString(event.getTranslation());

                const audioIcon = createElement('button', null, ['option_button']);
                translationView.appendChild(audioIcon);
                audioIcon.innerHTML = '<span class="material-symbols-outlined">brand_awareness</span>';
                audioIcon.onclick = () => spook(event, true);

                const translationCopyButton = createElement('button', null, ['option_button']);
                translationView.appendChild(translationCopyButton);
                translationCopyButton.innerHTML = '<span class="material-symbols-outlined">content_copy</span>';
                translationCopyButton.onclick = (_) => copyToClipboard(event.getTranslation());

                const translationDownloadButton = createElement('button', null, ['option_button']);
                translationView.appendChild(translationDownloadButton);
                translationDownloadButton.innerHTML = '<span class="material-symbols-outlined">download</span>';
                translationDownloadButton.onclick = (_) => downloadAsFile(event.getTranslation(), 'translation.txt');
            }
        }
    }
    return view;
}

window.openTab = openTab