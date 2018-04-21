tests
test057IdentifierAcceptsUnderscore
	| pp |
	pp := parser identifier end.
	self assert: '_' equals: (pp parse: '_').
	self assert: 'r_turn' equals: (pp parse: 'r_turn').
	self assert: 'a_' equals: (pp parse: 'a_').
	self assert: 'a_._b._._A_' equals: (pp parse: 'a_._b._._A_')