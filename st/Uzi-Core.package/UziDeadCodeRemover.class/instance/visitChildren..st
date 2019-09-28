visiting
visitChildren: node
	node children do: [:child | child ifNotNil: [self visit: child]]