
const { SoundServiceClient } = require('./proto/sound_transfer_grpc_web_pb.js')
const { SoundRequest, SoundResponse, TextMessage } = require('./proto/sound_transfer_pb.js')

const client = new SoundServiceClient('100.80.80.156' + ':50051', null, null)

module.exports = { SoundRequest, SoundResponse, TextMessage, client }