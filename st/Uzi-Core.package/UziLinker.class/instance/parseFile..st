private
parseFile: path
	| ast src |
	src := librariesDirectory readOnlyFileNamed: path do: [:file | file contents].
	ast := parser parse: src.
	ast isPetitFailure
		ifTrue: [(UziParseError failure: ast source: src file: path) signal].
	^ ast