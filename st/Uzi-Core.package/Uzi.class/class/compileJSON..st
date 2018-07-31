building
compileJSON: jsonString
	| ast |
	ast := self parseJSON: jsonString.
	ast := self resolveImports: ast.
	self check: ast.
	^ self compile: ast