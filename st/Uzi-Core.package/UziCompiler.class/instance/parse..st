compiling
parse: aString
	| ast |
	ast := UziOldParser parse: aString.
	ast isPetitFailure ifTrue: [^ self signalParseError: ast source: aString].
	^ ast