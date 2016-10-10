grammar-scripts
testParserExpectsEndOfInputAfterScripts

	| pp result |
	pp := UziParser new.
	result := pp parse: '
		#script1 ticking 1/s [foo].
		a'.
	self assert: result isPetitFailure 