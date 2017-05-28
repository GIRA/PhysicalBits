tests
testImportsAreParsedCorrectly
	| ast pp src |
	pp := UziParser new librariesDirectory: (FileDirectory on: 'D:\UAI\GIRA\Uzi\uzi\tests').
	src := 'import a = ''test.uzi''
		| var = 1 |
		@blink 1/s [D13 toggle]'.
	ast := pp parse: src.
	self assert: 2 equals: ast scripts size.
	self assert: 'a@foo' equals: ast scripts first name.
	self assert: 'blink' equals: ast scripts second name.
	self assert: 2 equals: ast variableDefinitions size.
	self assert: 'a@var' equals: ast variableDefinitions first name.
	self assert: 'var' equals: ast variableDefinitions second name	