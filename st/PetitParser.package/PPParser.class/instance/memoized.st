operators
memoized
	"Answer a new memoized parser, for refraining redundant computations. This ensures polynomial time O(n^4) for left-recursive grammars and O(n^3) for non left-recursive grammars in the worst case. Not necessary for most grammars that are carefully written and in O(n) anyway."
	
	^ PPMemoizedParser on: self