tests
testReferencingImportedScript
	| ast main pp src |
	pp := UziParser new librariesDirectory: (FileDirectory on: 'D:\UAI\GIRA\Uzi\uzi\tests').
	src := 'import a = ''test.uzi''
		@main ticking [
			a@foo start.
			a@foo stop.
			a@foo value]'.
	ast := pp parse: src.
	main := ast scripts detect: [:s | s name = 'main'].
	main body statements
		do: [:stmt | self assert: 'a@foo' equals: stmt scriptName]