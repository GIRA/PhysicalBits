grammar-literals
integer
	^ ($- asParser optional , digits) flatten map: [:val | Integer readFrom: val]