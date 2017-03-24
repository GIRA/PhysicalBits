tests
testParserExpectsEndOfInputAfterScripts
	| pp result |
	pp := parser.
	result := pp parse: '
		@script1 ticking 1/s [foo].
		a'.
	self assert: result isPetitFailure 