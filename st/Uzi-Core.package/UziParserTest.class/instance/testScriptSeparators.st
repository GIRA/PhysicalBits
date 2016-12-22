tests
testScriptSeparators
	| pp script scripts nodes |
	pp := parser.
	nodes := pp parse: '
		#script1 ticking 1/s [foo]... "Holaaaaa"... . . . . 
		..
		.
		#script2 ticking 2/m [bar]'.
	scripts := nodes second.
	self assert: scripts size equals: 2.
	script := scripts first.
	self assert: script name = #script1.
	self assert: script state = #ticking.
	self assert: script tickingRate delay = 1 second.
	self assert: script body notNil.
	self assert: script body size = 1.
	self assert: script body first name = 'foo'.
	script := scripts second.
	self assert: script name = #script2.
	self assert: script state = #ticking.
	self assert: script tickingRate delay = 30 seconds.
	self assert: script body notNil.
	self assert: script body size = 1.
	self assert: script body first name = 'bar'