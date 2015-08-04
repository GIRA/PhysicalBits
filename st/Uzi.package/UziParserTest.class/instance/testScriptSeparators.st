grammar-scripts
testScriptSeparators
	| pp script scripts |
	pp := UziParser new.
	scripts := pp parse: '
		#script1 ticking 1/s [foo]... "Holaaaaa"... . . . . 
		..
		.
		#script2 paused 2/m [bar]'.
	self assert: scripts size equals: 2.
	script := scripts first.
	self assert: script name = #script1.
	self assert: script state = #ticking.
	self assert: script tickingRate delay = 1 second.
	self assert: script body notNil.
	self assert: script body size = 1.
	self assert: script body first selector = 'foo'.
	script := scripts second.
	self assert: script name = #script2.
	self assert: script state = #paused.
	self assert: script tickingRate delay = 30 seconds.
	self assert: script body notNil.
	self assert: script body size = 1.
	self assert: script body first selector = 'bar'