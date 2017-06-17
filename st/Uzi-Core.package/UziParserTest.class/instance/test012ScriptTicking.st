tests
test012ScriptTicking
	| pp ast |
	pp := parser script end.
	ast := pp parse: 'task foo() ticking {}'.
	self assert: #foo equals: ast name.
	self assert: #ticking equals: ast state.
	self assert: nil equals: ast tickingRate