

const { SoundServiceClient } = require('./proto/sound_transfer/sound_transfer_grpc_web_pb.js')
const { SoundRequest, 
        SoundResponse, 
        TextMessage, 
        SoundStreamResponse, 
        SpeakerAndLine } = require('./proto/sound_transfer/sound_transfer_pb.js')

const { ClientServiceClient } = require('./proto/authentication/authentication_grpc_web_pb.js')
const { TextHistory, 
        UserCredits, 
        StatusResponse, 
        LoginResponse, 
        Empty } = require('./proto/authentication/authentication_pb.js')

const soundClient = new SoundServiceClient('http://100.80.80.156:50051')
const authenticationClient = new ClientServiceClient('http://100.80.80.156:50051')

module.exports = { soundClient, 
                   SoundRequest, 
                   SoundResponse, 
                   TextMessage,
                   SoundStreamResponse,
                   SpeakerAndLine,
                   authenticationClient,
                   TextHistory,
                   UserCredits,
                   StatusResponse,
                   LoginResponse,
                   Empty }