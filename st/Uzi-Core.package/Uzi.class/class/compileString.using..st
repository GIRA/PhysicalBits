building
compileString: aString using: aCompiler
	| ast |
	ast := self parse: aString.
	ast := self resolveImports: ast.
	self check: ast.
	^ aCompiler compile: ast