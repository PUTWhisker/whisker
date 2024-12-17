from google.protobuf.timestamp_pb2 import Timestamp
import datetime
import dicts
import re

class QuerryParameters():

    def __init__(
            self,
            translate:bool
    ):
        self.translate = translate
        self.startTime = None
        self.endTime = None
        self.limit = 10
        self.language = None
        self.translation_language = None


    def validateStartTime(self):
        regex = r'''^                             
                (19|20)[0-9][0-9].                      
                (0[1-9]|1[0-2]).                         
                (0[1-9]|[12][0-9]|3[01]).                            
                ([01][0-9]|2[0-3]).                           
                ([0-5][0-9]).                           
                ([0-5][0-9])$'''
        if re.match(regex, self.startTime, re.VERBOSE):
            return
        self.startTime = None


    def validateEndTime(self):
        regex = r'''^                             
                (19|20)[0-9][0-9].                      
                (0[1-9]|1[0-2]).                         
                (0[1-9]|[12][0-9]|3[01]).                            
                ([01][0-9]|2[0-3]).                           
                ([0-5][0-9]).                           
                ([0-5][0-9])$'''
        if re.match(regex, self.endTime, re.VERBOSE):
            return
        self.endTime = None


    def validateLanguage(self):
        for key, value in dicts.LANGUAGES.items():
            if self.language == key or self.language == value:
                print(self.language)
                self.language = key
                return
        self.language = None


    def validateTranslationLanguage(self):
        for key, value in dicts.LANGUAGES.items():
            if self.translation_language == key or self.translation_language == value:
                self.translation_language = key
                return
        self.translation_language = None
    

    def validateLimit(self) -> str:
        if self.limit.isdigit() and int(self.limit) > 0 and int(self.limit) <= 100:
            self.limit = int(self.limit)
            return
        self.limit = None
    

    def convertDate(self):
        if self.startTime:
            startTime = datetime.datetime(
                        int(self.startTime[0:4]),
                        int(self.startTime[5:7]),
                        int(self.startTime[8:10]),
                        int(self.startTime[11:13]),
                        int(self.startTime[14:16]),
                        int(self.startTime[17:19]),
                        tzinfo=datetime.datetime.now().astimezone().tzinfo
                )
            utcTime = startTime.astimezone(datetime.timezone.utc)
            self.startTime = Timestamp()
            self.startTime.FromDatetime(utcTime)
        if self.endTime:
            endTime = datetime.datetime(
                        int(self.endTime[0:4]),
                        int(self.endTime[5:7]),
                        int(self.endTime[8:10]),
                        int(self.endTime[11:13]),
                        int(self.endTime[14:16]),
                        int(self.endTime[17:19]),
                        tzinfo=datetime.datetime.now().astimezone().tzinfo
                )
            utcTime = endTime.astimezone(datetime.timezone.utc)
            self.endTime = Timestamp()
            self.endTime.FromDatetime(utcTime)
        return