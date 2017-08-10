utils
check: aString
	| ast |
	ast := UziParser parse: aString.
	ast isPetitFailure ifTrue: [^ ast].
	^ UziTypeChecker check: ast