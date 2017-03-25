tests
testLoops
	| pp ast statements toArray |
	pp := parser.
	ast := pp parse: '@script1 ticking 1/s 
		["1" [3 > 4] whileTrue: [D13 on].
		"2" [3 > 4] whileFalse: [D13 on].
		"3" [3 > 4] whileTrue.
		"4" [3 > 4] whileFalse.
		"5" [D13 on] doWhileTrue: [3 > 4].
		"6" [D13 on] doWhileFalse: [3 > 4]]'.
	statements := ast scripts first body statements.
	self assert: 6 equals: statements size.
	
	toArray := [:send |
		{ send receiver value . send selector . (send arguments at: 1 ifAbsent: nil) value}].
	
	statements first ifNotNil: [:while |
		self deny: while negated.
		self deny: while evaluateBodyFirst.
		self assert: #(3 > 4) equals: (toArray value: while condition statements first).
		self assert: #(13 on nil) equals: (toArray value: while body statements first)].
	
	statements second ifNotNil: [:while |
		self assert: while negated.
		self deny: while evaluateBodyFirst.
		self assert: #(3 > 4) equals: (toArray value: while condition statements first).
		self assert: #(13 on nil) equals: (toArray value: while body statements first)].
	
	statements third ifNotNil: [:while |
		self deny: while negated.
		self deny: while evaluateBodyFirst.
		self assert: #(3 > 4) equals: (toArray value: while condition statements first).
		self assert: while body isEmpty].
	
	statements fourth ifNotNil: [:while |
		self assert: while negated.
		self deny: while evaluateBodyFirst.
		self assert: #(3 > 4) equals: (toArray value: while condition statements first).
		self assert: while body isEmpty].
	
	statements fifth ifNotNil: [:while |
		self deny: while negated.
		self assert: while evaluateBodyFirst.
		self assert: #(13 on nil) equals: (toArray value: while body statements first).
		self assert: #(3 > 4) equals: (toArray value: while condition statements first)].
	
	statements sixth ifNotNil: [:while |
		self assert: while negated.
		self assert: while evaluateBodyFirst.
		self assert: #(13 on nil) equals: (toArray value: while body statements first).
		self assert: #(3 > 4) equals: (toArray value: while condition statements first)]