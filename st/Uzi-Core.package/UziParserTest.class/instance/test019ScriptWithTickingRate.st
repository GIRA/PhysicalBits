tests
test019ScriptWithTickingRate
	| pp ast |
	pp := parser script end.
	ast := pp parse: 'task foo() running 1/s {}'.
	self assert: #foo equals: ast name.
	self assert: #running equals: ast state.
	self assert: 1 equals: ast tickingRate value