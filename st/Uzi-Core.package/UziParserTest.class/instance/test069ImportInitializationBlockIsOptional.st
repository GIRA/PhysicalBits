tests
test069ImportInitializationBlockIsOptional
	| a b |
	a := parser parse: 'import foo from ''Foo.uzi'';'.
	b := parser parse: 'import foo from ''Foo.uzi'' { a = D10; b = 7; }'.
	self deny: (self compare: a with: b)
		description: 'trees should be different'