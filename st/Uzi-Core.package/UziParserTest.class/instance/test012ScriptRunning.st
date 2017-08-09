tests
test012ScriptRunning
	| pp ast |
	pp := parser script end.
	ast := pp parse: 'task foo() running {}'.
	self assert: #foo equals: ast name.
	self assert: #running equals: ast state.
	self assert: nil equals: ast tickingRate