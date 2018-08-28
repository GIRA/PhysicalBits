tests
test065OperatorPrecedence
	| actual expected |
	actual := parser expression parse: '8 + 3 + 4 + 5 + 6'.
	expected := call
		selector: #+ 
		arguments: {
			nil -> (call
				selector: #+ 
				arguments: {
					nil -> (call
						selector: #+ 
						arguments: {
							nil -> (call
								selector: #+ 
								arguments: {
									nil -> (number value: 8).
									nil -> (number value: 3)
								}).
							nil -> (number value: 4)
						}).
					nil -> (number value: 5)
				}).
			nil -> (number value: 6)
		}.
	self assert: expected equals: actual