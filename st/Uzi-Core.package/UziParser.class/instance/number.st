grammar-literals
number
	^ float / integer map: [:val | UziNumberNode value: val]