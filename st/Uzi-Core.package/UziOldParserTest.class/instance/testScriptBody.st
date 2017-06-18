tests
testScriptBody
	| body pp |
	pp := parser block.
	body := (pp parse: '[foo. bar. baz]') value.
	self assert: #(foo bar baz) equals: (body statements collect: #name)