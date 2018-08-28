tests
test062OperatorPrecedence
	| actual expected |
	actual := parser expression parse: '3 + 4 * 5'.
	expected := call 
		selector: #+
		arguments: {
			nil -> (number value: 3).
			nil -> (call
				selector: #*
				arguments: {
					nil -> (number value: 4).
					nil -> (number value: 5)
				})
		}.
	self assert: expected equals: actual