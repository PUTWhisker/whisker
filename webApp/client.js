
const { setupConnection} = require('./send_file.js')
const { setupRecord } = require('./record.js')

window.onload = function() {
    setupConnection()
    setupRecord()
}

