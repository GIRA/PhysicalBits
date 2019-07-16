reporting
setGlobal: globalName report: reportEnabled
	| globalNumber |
	globalNumber := self getGlobalNumber: globalName.
	^ self send: {
		self class msgOutSetGlobalReport. 
		globalNumber. 
		reportEnabled ifTrue: [1] ifFalse: [0]
	}