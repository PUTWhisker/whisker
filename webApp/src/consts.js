import serverParams from './configuration.json'
console.log(serverParams.host)

export const { SoundServiceClient } = require('../proto/sound_transfer/sound_transfer_grpc_web_pb.js')
export const { TextMessage, 
        TranscriptionRequest, 
        TranslationRequest, 
        TranscirptionLiveRequest, 
        SoundResponse,
        SoundStreamResponse,
        SpeakerAndLineResponse } = require('../proto/sound_transfer/sound_transfer_pb.js')

export const { ClientServiceClient } = require('../proto/authentication/authentication_grpc_web_pb.js')
export const { TranscriptionHistory, 
        TranslationHistory, 
        DiarizationHistory, 
        UserCredits, 
        LoginResponse, 
        Id,
        NewTranscription, 
        NewTranslation, 
        NewDiarization,
        QueryParamethers } = require('../proto/authentication/authentication_pb.js')

export const soundClient = new SoundServiceClient(`${serverParams.host}:${serverParams.port}`)
export const authenticationClient = new ClientServiceClient(`${serverParams.host}:${serverParams.port}`)