tests
test067UnaryOperatorsHaveHigherPrecedence
	| actual expected |
	actual := parser expression end parse: '1 + !1'.
	expected := call
		selector: #+
		arguments: {
			nil -> (number value: 1).
			nil -> (call
				selector: #!
				arguments: { nil -> (number value: 1) })
		}.
	self assert: expected equals: actual