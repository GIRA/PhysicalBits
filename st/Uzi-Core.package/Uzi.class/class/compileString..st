building
compileString: aString
	| ast |
	ast := self parse: aString.
	ast := self resolveImports: ast.
	self check: ast.
	^ self compile: ast