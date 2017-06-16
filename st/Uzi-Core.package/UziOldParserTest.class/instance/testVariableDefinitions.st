tests
testVariableDefinitions
	| pp ast vars |
	pp := parser.
	ast := pp parse: '
		| d, a = 4, b = 3, c |
		@script1 ticking 1/s [foo]'.
	vars := ast variableDefinitions.
	self assert: vars size equals: 4.
	self assert: #d equals: vars first name.
	self assert: #a equals: vars second name.
	self assert: #b equals: vars third name.
	self assert: #c equals: vars fourth name.
	self assert: 0 equals: vars first value.
	self assert: 4 equals: vars second value.
	self assert: 3 equals: vars third value.
	self assert: 0 equals: vars fourth value
	