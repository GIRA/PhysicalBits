building
resolveImports: ast
	| linker |
	linker := self defaultLinker
		parser: self defaultParser new
		librariesDirectory: self librariesDirectory.
	^ linker applyOn: ast