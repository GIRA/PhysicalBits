grammar-scripts
testScriptBody
	| body pp |
	pp := UziParser new scriptBody.
	body := pp parse: '[foo. bar. baz]'.
	self assert: body size = 3.
	self assert: (body allSatisfy: [:each | each receiver = nil]).
	self assert: (body allSatisfy: [:each | each arguments isEmpty]).
	self assert: body first selector = 'foo'.
	self assert: body second selector = 'bar'.
	self assert: body third selector = 'baz'.
	