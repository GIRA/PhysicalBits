tests
testSpecialVariables
	| pp ast |
	pp := parser specialVariable.
	ast := pp parse: '#time'.
	self assert: #time equals: ast selector.
	self assert: 0 equals: ast arguments size