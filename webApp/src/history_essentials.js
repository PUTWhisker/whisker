const flags = {
    'pl': '🇵🇱',
    'en-UK': '🇬🇧',
    'es-ES': '🇪🇸'
};
const sampleData1 = {
    'history': [
        {
            'date': '24.12.2024',
            'file_size': '25kB',
            'title': 'recipe_diarization',
            'transcription_result': 'Speaker001: Z podanej ilości składników otrzymasz całą kaczkę pieczoną z farszem z jabłek i żurawiny. Będą to cztery duże lub sześć mniejszych porcji.\nSpeaker002: Delikatesy.',
            'selected_options': {
                'original_language': 'pl',
                'multiple_speakers': true
            }
        }
    ]
};
const sampleData2 = {
    'history': [
        {
            'date': '04.12.2024',
            'file_size': '6kB',
            'title': 'recipe_transcription',
            'transcription_result': 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent aliquam et felis non rhoncus. Sed id risus sollicitudin, vulputate erat accumsan, iaculis sapien. Donec faucibus arcu at erat pretium posuere. Etiam in sem nulla. Vivamus porttitor enim ante, non sodales ante posuere a. Maecenas lacinia ligula sit amet nibh maximus egestas. Suspendisse potenti. Sed dictum eu lectus vitae dictum. Donec vel vehicula libero, nec scelerisque diam. Duis vel ultricies ex. Nulla congue mattis libero, et lobortis ipsum tincidunt non. Etiam nec quam eget lectus auctor auctor. In accumsan, arcu hendrerit euismod aliquam, nisi eros condimentum orci, id interdum ante risus eget sem.',
            'selected_options': {
                'original_language': 'en-UK',
                'multiple_speakers': false
            }
        }
    ]
};
const sampleData3 = {
    'history': [
        {
            'date': '24.12.2024',
            'file_size': '25kB',
            'title': 'recipe_translation',
            'transcription_result': 'Z podanej ilości składników otrzymasz całą kaczkę pieczoną z farszem z jabłek i żurawiny. Będą to cztery duże lub sześć mniejszych porcji.',
            'translation_result': 'The given amount of ingredients will give you a whole duck baked with apple and cranberry stuffing. These will be four large or six smaller portions.',
            'selected_options': {
                'original_language': 'pl',
                'translate_language': 'en-UK',
                'multiple_speakers': false
            }
        }
    ]
};

function getTranslationHistory() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve(JSON.stringify(sampleData3));
        }, 10);
    });
}

function getTranscribeHistory() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve(JSON.stringify(sampleData2));
        }, 10);
    });
}

function getDiarizationHistory() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve(JSON.stringify(sampleData1));
        }, 10);
    });
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

function speak(event, translation) {
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

// function createView(event) {

//     const view = createElement('div', null, ['event_view']);

//     const dateView = createElement('div', null, ['event_date_view']);
//     view.appendChild(dateView);
//     dateView.innerText = event.date;

//     const titleView = createElement('div', null, ['event_title']);
//     view.appendChild(titleView);
//     titleView.innerText = event.title;

//     const previewsView = createElement('div', null, ['event_previews_view']);
//     view.appendChild(previewsView);
//     {
//         const transcriptionView = createElement('div', null, ['event_text_row']);
//         previewsView.appendChild(transcriptionView);
//         {
//             const transcriptionflag = createElement('div', null, ['text_flag']);
//             transcriptionView.appendChild(transcriptionflag);
//             transcriptionflag.innerText = flags[event.selected_options.original_language];

//             const transcriptionViewText = createElement('div', null, ['preview_text']);
//             transcriptionView.appendChild(transcriptionViewText);
//             transcriptionViewText.innerText = clipString(event.transcription_result);

//             const audioIcon = createElement('button', null, ['option_button']);
//             transcriptionView.appendChild(audioIcon);
//             audioIcon.innerHTML = '<span class="material-symbols-outlined">brand_awareness</span>';
//             audioIcon.onclick = () => spook(event, false);

//             const transcriptionCopyButton = createElement('button', null, ['option_button']);
//             transcriptionView.appendChild(transcriptionCopyButton);
//             transcriptionCopyButton.innerHTML = '<span class="material-symbols-outlined">content_copy</span>';
//             transcriptionCopyButton.onclick = (_) => copyToClipboard(event.transcription_result);

//             const transcriptionDownloadButton = createElement('button', null, ['option_button']);
//             transcriptionView.appendChild(transcriptionDownloadButton);
//             transcriptionDownloadButton.innerHTML = '<span class="material-symbols-outlined">download</span>';
//             transcriptionDownloadButton.onclick = (_) => downloadAsFile(event.transcription_result, 'transcription.txt');
//         }

//         if (event.translation_result) {
//             const translationView = createElement('div', null, ['event_text_row']);
//             previewsView.appendChild(translationView);
//             {
//                 const translationflag = createElement('div', null, ['text_flag']);
//                 translationView.appendChild(translationflag);
//                 translationflag.innerText = flags[event.selected_options.translate_language];

//                 const translationViewText = createElement('div', null, ['preview_text']);
//                 translationView.appendChild(translationViewText);
//                 translationViewText.innerText = clipString(event.translation_result);

//                 const audioIcon = createElement('button', null, ['option_button']);
//                 translationView.appendChild(audioIcon);
//                 audioIcon.innerHTML = '<span class="material-symbols-outlined">brand_awareness</span>';
//                 audioIcon.onclick = () => spook(event, true);

//                 const translationCopyButton = createElement('button', null, ['option_button']);
//                 translationView.appendChild(translationCopyButton);
//                 translationCopyButton.innerHTML = '<span class="material-symbols-outlined">content_copy</span>';
//                 translationCopyButton.onclick = (_) => copyToClipboard(event.translation_result);

//                 const translationDownloadButton = createElement('button', null, ['option_button']);
//                 translationView.appendChild(translationDownloadButton);
//                 translationDownloadButton.innerHTML = '<span class="material-symbols-outlined">download</span>';
//                 translationDownloadButton.onclick = (_) => downloadAsFile(event.translation_result, 'translation.txt');
//             }
//         }
//     }

//     return view;
// }

document.addEventListener('DOMContentLoaded', (e) => {

    getTranslationHistory().then((historyJSON) => {
        const history = JSON.parse(historyJSON).history;
        history.forEach(element => {
            const html = njTemplate.render({ event: element, flags: flags, clipString: clipString });
            document.getElementById("transcription_history").innerHTML += html;
        });
    });
    getTranscribeHistory().then((historyJSON) => {
        const history = JSON.parse(historyJSON).history;
        history.forEach(element => {
            const html = njTemplate.render({ event: element, flags: flags, clipString: clipString });
            document.getElementById("translation_history").innerHTML += html;
        });
    });
    getDiarizationHistory().then((historyJSON) => {
        const history = JSON.parse(historyJSON).history;
        history.forEach(element => {
            const html = njTemplate.render({ event: element, flags: flags, clipString: clipString });
            document.getElementById("diarization_history").innerHTML += html;
        });
    });

});