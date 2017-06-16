tests
testImportScriptReferencesAreAliased
	| ast foo pp src |
	pp := UziOldParser new librariesDirectory: (FileDirectory on: 'D:\UAI\GIRA\Uzi\uzi\tests').
	src := 'import a = ''test3.uzi''
		@main ticking [D13 toggle]'.
	ast := pp parse: src.
	foo := ast scripts detect: [:s | s name = 'a@foo'].
	foo body statements do: [:stmt | self assert: 'a@bar' equals: stmt scriptName]