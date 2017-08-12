tests
test048ProgramWithImports
	| program |
	program := parser parse: '
		import foo = ''Foo.uzi'';
		import bar = ''Bar.uzi'';
		task main() {}'.
	self assert: 2 equals: program imports size.
	self assert: 1 equals: program scripts size.
	self assert: 0 equals: program globals size.
	self assert: #foo equals: program imports first alias.
	self assert: #bar equals: program imports second alias.
	self assert: 'Foo.uzi' equals: program imports first path.
	self assert: 'Bar.uzi' equals: program imports second path