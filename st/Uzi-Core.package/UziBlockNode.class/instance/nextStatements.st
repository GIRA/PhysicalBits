accessing
nextStatements
	^ self statements
		ifEmpty: [#()]
		ifNotEmpty: [:stmts | { stmts first }]