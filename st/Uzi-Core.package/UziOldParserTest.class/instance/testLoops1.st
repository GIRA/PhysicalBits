tests
testLoops1
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
		{ send arguments first value . send selector . (send arguments at: 2 ifAbsent: nil) value}].
	
	statements first ifNotNil: [:while |
		self deny: while negated.
		self assert: while pre isEmpty.
		self assert: #(3 > 4) equals: (toArray value: while condition).
		self assert: #(13 on nil) equals: (toArray value: while post statements first)].
	
	statements second ifNotNil: [:while |
		self assert: while negated.
		self assert: while pre isEmpty.
		self assert: #(3 > 4) equals: (toArray value: while condition).
		self assert: #(13 on nil) equals: (toArray value: while post statements first)].
	
	statements third ifNotNil: [:while |
		self deny: while negated.
		self assert: while pre isEmpty.
		self assert: #(3 > 4) equals: (toArray value: while condition).
		self assert: while post isEmpty].
	
	statements fourth ifNotNil: [:while |
		self assert: while negated.
		self assert: while pre isEmpty.
		self assert: #(3 > 4) equals: (toArray value: while condition).
		self assert: while post isEmpty].
	
	statements fifth ifNotNil: [:while |
		self deny: while negated.
		self assert: #(13 on nil) equals: (toArray value: while pre statements first).
		self assert: #(3 > 4) equals: (toArray value: while condition).
		self assert: while post isEmpty].
	
	statements sixth ifNotNil: [:while |
		self assert: while negated.
		self assert: #(13 on nil) equals: (toArray value: while pre statements first).
		self assert: #(3 > 4) equals: (toArray value: while condition).
		self assert: while post isEmpty]