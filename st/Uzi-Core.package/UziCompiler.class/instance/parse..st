compiling
parse: aString
	| ast |
	ast := UziParser parse: aString.
	ast isPetitFailure ifTrue: [^ self signalParseError: ast source: aString].
	^ ast