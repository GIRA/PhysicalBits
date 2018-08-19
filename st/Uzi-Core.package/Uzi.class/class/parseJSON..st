building
parseJSON: jsonString
	| ast src |
	ast := UziJSON decode: jsonString.
	src := UziParseNodePrinter new print: ast.
	self flag: #Richo. "HACK! I should set all tokens, not just the top node.."
	ast token: (PPToken 
		on: src
		start: 1
		stop: src size
		value: nil).
	^ ast