building
parse: aString
	| ast |
	ast := self defaultParser new parse: aString.	
	ast isPetitFailure ifTrue: [^ (UziParseError failure: ast source: aString) signal].
	^ ast