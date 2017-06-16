tests
test008EmptyScript
	| pp ast |
	pp := parser script end.
	ast := pp parse: 'task foo() {}'.
	self assert: #foo equals: ast name.
	self assert: nil equals: ast state.
	self assert: nil equals: ast tickingRate