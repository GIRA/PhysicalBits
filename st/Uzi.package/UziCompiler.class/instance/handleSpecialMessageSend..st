visiting
handleSpecialMessageSend: node
	^ node selector caseOf: {
		[#ifTrue:] -> [self handleIfTrueMessageSend: node. ^ true].
		[#ifTrue:ifFalse:] -> [self handleIfTrueIfFalseMessageSend: node. ^ true]
	} otherwise: [false].