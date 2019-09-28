building
compileJSON: jsonString using: aCompiler
	| ast |
	ast := self parseJSON: jsonString.
	ast := self resolveImports: ast.
	self check: ast.
	^ aCompiler compile: ast