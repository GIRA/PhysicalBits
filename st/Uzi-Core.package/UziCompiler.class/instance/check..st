compiling
check: ast
	| errors |
	errors := UziParseTreeValidator check: ast.
	errors ifNotEmpty: [(UziCompileError errors: errors) signal]