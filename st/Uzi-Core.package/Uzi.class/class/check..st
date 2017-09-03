building
check: ast
	| errors |
	errors := self defaultParseTreeValidator new check: ast.	
	errors ifNotEmpty: [(UziCompileError errors: errors) signal]