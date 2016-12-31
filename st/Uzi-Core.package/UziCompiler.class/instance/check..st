compiling
check: ast
	| errors |
	errors := UziTypeChecker check: ast.
	errors ifNotEmpty: [(UziCompileError errors: errors) signal]