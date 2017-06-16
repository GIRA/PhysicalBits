tests
test002Constant
	| pp |	
	pp := parser constant end.
	self assert: (pp parse: 'D5') value = 5.
	self assert: (pp parse: 'A1') value = 15.