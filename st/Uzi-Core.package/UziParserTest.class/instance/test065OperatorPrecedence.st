tests
test065OperatorPrecedence
	| actual expected |
	actual := parser expression parse: '8 + 3 + 4 + 5 + 6'.
	expected := prim
		selector: #+ 
		arguments: {
			prim 
				selector: #+ 
				arguments: {
					prim
						selector: #+ 
						arguments: {
							prim 
								selector: #+ 
								arguments: {
									number value: 8.
									number value: 3.
								} 
								primitive: (UziPrimitive named: #add).
							number value: 4
						} 
						primitive: (UziPrimitive named: #add).
					number value: 5
				} 
				primitive: (UziPrimitive named: #add).
			number value: 6
		} 
		primitive: (UziPrimitive named: #add).
	self assert: expected equals: actual