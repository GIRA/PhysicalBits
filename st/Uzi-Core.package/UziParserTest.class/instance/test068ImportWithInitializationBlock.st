tests
test068ImportWithInitializationBlock
	| ast pp |
	pp := parser import end.
	ast := pp parse: 'import foo from ''Foo.uzi'' { a = D10; b = 7; }'.
	self assert: #foo equals: ast alias.
	self assert: 'Foo.uzi' equals: ast path.
	self assert: ast initializationBlock notNil