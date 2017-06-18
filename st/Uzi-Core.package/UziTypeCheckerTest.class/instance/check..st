utils
check: aString
	| ast |
	ast := UziOldParser parse: aString.
	ast isPetitFailure ifTrue: [^ ast].
	^ UziTypeChecker check: ast