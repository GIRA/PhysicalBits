tests
test019ScriptWithTickingRate
	| pp ast |
	pp := parser script end.
	ast := pp parse: 'task foo() ticking 1/s {}'.
	self assert: #foo equals: ast name.
	self assert: #ticking equals: ast state.
	self assert: 1 equals: ast tickingRate value