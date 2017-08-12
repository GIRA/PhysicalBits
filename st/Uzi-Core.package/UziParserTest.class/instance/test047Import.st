tests
test047Import
	| ast pp |
	pp := parser import end.
	ast := pp parse: 'import foo = ''Foo.uzi'';'.
	self assert: #foo equals: ast alias.
	self assert: 'Foo.uzi' equals: ast path