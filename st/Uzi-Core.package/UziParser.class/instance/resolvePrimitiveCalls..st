private
resolvePrimitiveCalls: ast
	ast select: [:node | node isCall and: [(ast scriptNamed: node selector) isNil]]
		thenDo: [:node | node primitiveName: (ast primitiveForSelector: node selector)]