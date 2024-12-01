

const { SoundServiceClient } = require('./proto/sound_transfer/sound_transfer_grpc_web_pb.js')
const { TextMessage, 
        TranscriptionRequest, 
        TranslationRequest, 
        TranscirptionLiveRequest, 
        SoundResponse,
        SoundStreamResponse,
        SpeakerAndLineResponse } = require('./proto/sound_transfer/sound_transfer_pb.js')

const { ClientServiceClient } = require('./proto/authentication/authentication_grpc_web_pb.js')
const { TextHistory, 
        UserCredits, 
        StatusResponse, 
        LoginResponse, 
        Empty } = require('./proto/authentication/authentication_pb.js')

const soundClient = new SoundServiceClient('http://127.0.0.1:50051')
const authenticationClient = new ClientServiceClient('http://127.0.0.1:50051')

module.exports = {  soundClient, 
                    TextMessage, 
                    TranscriptionRequest, 
                    TranslationRequest, 
                    TranscirptionLiveRequest, 
                    SoundResponse,
                    SoundStreamResponse,
                    SpeakerAndLineResponse,
                    authenticationClient,
                    TextHistory,
                    UserCredits,
                    StatusResponse,
                    LoginResponse,
                    Empty}