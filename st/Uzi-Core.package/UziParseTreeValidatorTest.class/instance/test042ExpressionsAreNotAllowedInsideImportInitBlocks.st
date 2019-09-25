tests
test042ExpressionsAreNotAllowedInsideImportInitBlocks
	self assertInvalid: 'import a from ''A.uzi'' { a = 3 + 4; }'.
	self assertValid: 'import a from ''A.uzi'' {}'.
	self assertValid: 'import a from ''A.uzi'';'.
	self assertValid: 'import a from ''A.uzi'' { a = 4; }'.