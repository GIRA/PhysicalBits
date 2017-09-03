building
resolveImports: ast
	| ir |
	ir := self defaultImportResolver 
		parser: self defaultParser new
		librariesDirectory: self librariesDirectory.
	^ ir applyOn: ast