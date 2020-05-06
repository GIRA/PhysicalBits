visiting
visitNumberLiteralNode: node
	self saveIntervalFor: node
		during: [self write: '(ast/literal-number-node {1})' format: { node value }]