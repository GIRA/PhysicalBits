private
compareChildren: node
	node children do: [:each | each ifNotNil: [self visit: each]]