compiling
compile: aString 
	| ast |
	ast := self parse: aString.
	self check: ast.
	^ self visit: ast