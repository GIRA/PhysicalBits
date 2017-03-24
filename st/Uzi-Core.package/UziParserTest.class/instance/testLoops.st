tests
testLoops
	| pp ast statements toArray |
	pp := parser.
	ast := pp parse: '@script1 ticking 1/s 
		[[3 > 4] 
			whileTrue: [D13 on]]'.
	statements := ast scripts first body statements.
	
	toArray := [:send |
		{ send receiver value . send selector . (send arguments at: 1 ifAbsent: nil) value}].
	
	statements first ifNotNil: [:while |
		self assert: #(3 > 4) equals: (toArray value: while condition statements first).
		self assert: #(13 on nil) equals: (toArray value: while body statements first)]