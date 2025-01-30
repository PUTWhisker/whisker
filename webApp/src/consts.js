
const { SoundServiceClient } = require('../proto/sound_transfer/sound_transfer_grpc_web_pb.js')
const { TextMessage, 
        TranscriptionRequest, 
        TranslationRequest, 
        TranscirptionLiveRequest, 
        SoundResponse,
        SoundStreamResponse,
        SpeakerAndLineResponse } = require('../proto/sound_transfer/sound_transfer_pb.js')

const { ClientServiceClient } = require('../proto/authentication/authentication_grpc_web_pb.js')
const { TranscriptionHistory, 
        TranslationHistory, 
        DiarizationHistory, 
        UserCredits, 
        LoginResponse, 
        Id,
        NewTranscription, 
        NewTranslation, 
        NewDiarization,
        QueryParamethers } = require('../proto/authentication/authentication_pb.js')

const soundClient = new SoundServiceClient('http://whisper.cs.put.poznan.pl:80')
const authenticationClient = new ClientServiceClient('http://whisper.cs.put.poznan.pl:80')

module.exports = {  soundClient, 
                    TextMessage, 
                    TranscriptionRequest, 
                    TranslationRequest, 
                    TranscirptionLiveRequest, 
                    SoundResponse,
                    SoundStreamResponse,
                    SpeakerAndLineResponse,
                    authenticationClient,
                    TranscriptionHistory, 
                    TranslationHistory, 
                    DiarizationHistory, 
                    UserCredits, 
                    LoginResponse, 
                    Id,
                    NewTranscription, 
                    NewTranslation, 
                    NewDiarization,
                    QueryParamethers}