reporting
setGlobal: globalIndex report: reportEnabled
	^ self send: {
		self class msgOutSetGlobalReport. 
		globalIndex. 
		reportEnabled ifTrue: [1] ifFalse: [0]
	}