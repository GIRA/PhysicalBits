tests
test064OperatorPrecedence
	| actual expected |
	actual := parser expression parse: '8 == 3 + 4 * 5'.
	expected := prim 
		selector: #==
		arguments: {
			number value: 8.
			prim 
				selector: #+
				arguments: {
					number value: 3.
					prim 
						selector: #*
						arguments: {
							number value: 4.
							number value: 5
						}
						primitive: (UziPrimitive named: #multiply)
				}
				primitive: (UziPrimitive named: #add).
		}		
		primitive: (UziPrimitive named: #equals).
	self assert: expected equals: actual