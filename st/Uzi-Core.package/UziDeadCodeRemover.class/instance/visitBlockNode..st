visiting
visitBlockNode: node
	node children do: [:child | self visit: child]