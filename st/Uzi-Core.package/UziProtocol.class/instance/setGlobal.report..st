reporting
setGlobal: globalName report: reportEnabled
	(self getGlobalNumber: globalName)
		ifNotNil: [:globalNumber |
			reportEnabled
				ifTrue: [self globalsReporting add: globalName]
				ifFalse: [self globalsReporting remove: globalName ifAbsent: ["Do nothing"]].
			^ self send: {
				self class msgOutSetGlobalReport. 
				globalNumber. 
				reportEnabled ifTrue: [1] ifFalse: [0]
			}]