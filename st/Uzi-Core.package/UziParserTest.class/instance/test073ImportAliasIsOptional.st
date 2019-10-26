tests
test073ImportAliasIsOptional
	| a b |
	a := parser import parse: 'import ''Foo.uzi'';'.
	self assert: nil equals: a alias.
	self assert: 'Foo.uzi' equals: a path.
	self assert: nil equals: a initializationBlock.
	b := parser import parse: 'import ''Foo.uzi'' { a = D10; b = 7; }'.
	self assert: nil equals: b alias.
	self assert: 'Foo.uzi' equals: b path.
	self assert: b initializationBlock notNil.