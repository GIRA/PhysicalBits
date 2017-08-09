tests
test042VariableReferences
	| ast pp |
	pp := parser statement end.
	ast := pp parse: 'toggle(a);'.
	self assert: #toggle equals: ast selector.
	self assert: #a equals: ast arguments first name