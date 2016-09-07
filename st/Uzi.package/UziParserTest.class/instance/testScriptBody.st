grammar-scripts
testScriptBody
	| body pp |
	pp := UziParser new scriptBody.
	body := pp parse: '[foo. bar. baz]'.
	self assert: #(foo bar baz) equals: (body collect: #name)