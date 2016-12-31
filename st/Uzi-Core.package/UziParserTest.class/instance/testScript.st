tests
testScript
	| body pp script |
	pp := parser script.
	script := pp parse: '#main ticking 1/s [foo. bar. baz]'.
	self assert: script name = #main.
	self assert: script state = #ticking.
	self assert: script tickingRate delay = 1 second.
	self assert: script body notNil.
	body := script body statements.
	self assert: #(foo bar baz) equals: (body collect: #name)