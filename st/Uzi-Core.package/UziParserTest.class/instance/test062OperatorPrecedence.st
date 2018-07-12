tests
test062OperatorPrecedence
	| actual expected |
	actual := parser expression parse: '3 + 4 * 5'.
	expected := prim 
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
	self assert: expected equals: actual