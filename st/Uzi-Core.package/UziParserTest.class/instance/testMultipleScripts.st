tests
testMultipleScripts
	| pp script scripts ast |
	pp := parser.
	ast := pp parse: '
		@script1 ticking 1/s [foo]
		@script2 2/m [bar]'.
	scripts := ast scripts.
	self assert: scripts size equals: 2.
	script := scripts first.
	self assert: script name = #script1.
	self assert: script state = #ticking.
	self assert: script tickingRate delay = 1 second.
	self assert: script body statements notNil.
	self assert: script body statements size = 1.
	self assert: script body statements first name = 'foo'.
	script := scripts second.
	self assert: script name = #script2.
	self assert: script state isNil.
	self assert: script tickingRate delay = 30 seconds.
	self assert: script body statements notNil.
	self assert: script body statements size = 1.
	self assert: script body statements first name = 'bar'