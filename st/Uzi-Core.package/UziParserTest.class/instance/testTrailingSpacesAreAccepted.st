tests
testTrailingSpacesAreAccepted
	| pp ast |
	pp := parser.
	ast := pp parse: '#script1 [D13 toggle] 		
			'.
	self assert: ast isPetitFailure not