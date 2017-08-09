compiling
parse: aString
	| ast |
	ast := parser parse: aString.
	ast isPetitFailure ifTrue: [^ self signalParseError: ast source: aString].
	^ ast