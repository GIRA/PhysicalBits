grammar-scripts
testEmptyScript
	| pp script |
	pp := UziParser new script.
	script := pp parse: '#foo ticking 8 / s []'.
	self deny: script isPetitFailure.
	self assert: script name = #foo.
	self assert: script state = #ticking.
	self assert: script tickingRate delay = 125 milliSeconds.
	script := pp parse: '    #foo    ticking      8 / s     []   '.
	self deny: script isPetitFailure.
	self assert: script name = #foo.
	self assert: script state = #ticking.
	self assert: script tickingRate delay = 125 milliSeconds