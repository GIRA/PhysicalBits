private
missingNodes: selectors
	^ nodes reject: [:node || selector |
		selector := ('visit', (node name allButFirst: 3), ':') asSymbol.
		selectors includes: selector].