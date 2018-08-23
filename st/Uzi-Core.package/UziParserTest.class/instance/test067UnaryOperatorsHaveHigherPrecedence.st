tests
test067UnaryOperatorsHaveHigherPrecedence
	| actual expected |
	actual := parser expression end parse: '1 + !1'.
	expected := prim
		selector: #+
		arguments: {
			number value: 1.
			prim 
				selector: #!
				arguments: { number value: 1}
				primitiveName: #negate
		} 
		primitiveName: #add.
	self assert: expected equals: actual