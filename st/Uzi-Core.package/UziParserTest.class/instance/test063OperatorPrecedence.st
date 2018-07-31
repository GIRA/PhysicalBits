tests
test063OperatorPrecedence
	| actual expected |
	actual := parser expression parse: '3 * 4 + 5'.
	expected := prim 
		selector: #+
		arguments: {
			prim 
				selector: #*
				arguments: {
					number value: 3.
					number value: 4
				}
				primitiveName: #multiply.
			number value: 5.
		}		
		primitiveName: #add.
	self assert: expected equals: actual