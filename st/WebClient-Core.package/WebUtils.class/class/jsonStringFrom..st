json-decode
jsonStringFrom: stream
	"Decodes a JSON encoded string"

	| ch result |
	(ch := stream next) = $" 
		ifFalse:[^self error: 'String expected'].
	result := WriteStream on: (String new: 20).
	[(ch := stream next) == nil] whileFalse:[
		ch = $" ifTrue:[^result contents].
		ch = $\ ifTrue:[ch := self jsonCharFrom: stream].
		result nextPut: ch.
	].
	^self error: 'Unterminated string'