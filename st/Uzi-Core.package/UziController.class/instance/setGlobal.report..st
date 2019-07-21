reporting
setGlobal: globalName report: reportEnabled
	(self getGlobalNumber: globalName)
		ifNotNil: [:globalNumber || msg |
			reportEnabled
				ifTrue: [self globalsReporting add: globalName]
				ifFalse: [self globalsReporting remove: globalName ifAbsent: ["Do nothing"]].
			msg := self send: {
				self class msgOutSetGlobalReport. 
				globalNumber. 
				reportEnabled ifTrue: [1] ifFalse: [0]
			}.
			self triggerEvent: UziEvents reportingUpdate.
			^ msg]