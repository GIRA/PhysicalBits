building
parseJSON: jsonString
	| ast src printer |
	ast := UziJSON decode: jsonString.
	printer := UziParseNodePrinter new.
	src := printer print: ast.
	ast do: [:node | (printer intervalAt: node)
		ifNotNil: [:interval || token |
			token := PPToken on: src
				start: interval first
				stop: interval last
				value: nil.
			node token: token]].
	^ ast