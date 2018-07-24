tests
test066OperatorPrecedence
	| actual expected |
	actual := parser expression parse: '1/2'.
	expected := prim
		selector: #/ 
		arguments: {
			number value: 1.
			number value: 2
		} 
		primitive: (UziPrimitive named: #divide).
	self assert: expected equals: actual