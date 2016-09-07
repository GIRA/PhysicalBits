visiting
handleSpecialMessageSend: node
	"
	If the selector is a special message then generate the corresponding code.
	Returns true if the message was special and false otherwise.
	"
	node selector caseOf: {
		[#ifTrue:] -> [self handleIfTrueMessageSend: node. ^ true].
		[#ifFalse:] -> [self handleIfFalseMessageSend: node. ^ true].
		[#ifTrue:ifFalse:] -> [self handleIfTrueIfFalseMessageSend: node. ^ true].
		[#ifFalse:ifTrue:] -> [self handleIfFalseIfTrueMessageSend: node. ^ true].
		[#start] -> [self handleStartScriptMessageSend: node. ^ true].
		[#stop] -> [self handleStopScriptMessageSend: node. ^ true]
	} otherwise: [^ false].