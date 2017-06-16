tests
testSpecialVariables
	| pp ast |
	pp := parser specialVariable.
	ast := pp parse: '#time'.
	self assert: #time equals: ast name