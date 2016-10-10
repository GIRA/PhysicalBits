grammar-scripts
testScript
	| body pp script |
	pp := UziParser new script.
	script := pp parse: '#main paused 1/s [foo. bar. baz]'.
	self assert: script name = #main.
	self assert: script state = #paused.
	self assert: script tickingRate delay = 1 second.
	self assert: script body notNil.
	body := script body.
	self assert: #(foo bar baz) equals: (body collect: #name)