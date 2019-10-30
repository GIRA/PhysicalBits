building
check: ast
	| errors |
	errors := self defaultParseTreeValidator new check: ast.	
	errors ifNotEmpty: [(UziAggregateError errors: errors) signal]