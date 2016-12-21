json-decode
jsonArrayFrom: stream
	"Decodes a JSON [value, *] array from the stream"

	| ch result |
	(ch := stream next) = $[ ifFalse:[^self error: 'JSON Array expected'].
	stream skipSeparators.
	stream peek = $] ifTrue:[stream next. ^#()].
	result := WriteStream on: (Array new: 10).
	["Decode the next value"
	stream skipSeparators.
	result nextPut: (self jsonDecode: stream).
	stream skipSeparators.
	(ch := stream next) = $]] whileFalse:[
		ch = $, ifFalse:[^self error: 'Comma expected'].
	].

	^result contents