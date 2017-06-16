tests
testImportVariableReferencesAreAliased
	| ast main pp src |
	pp := UziOldParser new librariesDirectory: (FileDirectory on: 'D:\UAI\GIRA\Uzi\uzi\tests').
	src := 'import a = ''test2.uzi''
		@main ticking [D13 toggle]'.
	ast := pp parse: src.
	main := ast scripts detect: [:s | s name = 'a@foo'].
	self assert: 'a@var' equals: main body statements first value name