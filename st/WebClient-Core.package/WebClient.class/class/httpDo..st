methods
httpDo: aBlock
	"Simplified wrapper for running various methods"

	| client response |
	client := self new.
	response := WebUtils handleAuth: [aBlock value: client].
	response content; close.
	^response