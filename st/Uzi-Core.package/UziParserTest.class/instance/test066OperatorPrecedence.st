tests
test066OperatorPrecedence
	| actual expected |
	actual := parser expression parse: '1/2'.
	expected := call
		selector: #/ 
		arguments: {
			nil -> (number value: 1).
			nil -> (number value: 2)
		}.
	self assert: expected equals: actual