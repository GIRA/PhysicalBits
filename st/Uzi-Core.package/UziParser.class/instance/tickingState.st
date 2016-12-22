grammar-scripts
tickingState
	^ 'ticking' asParser flatten map: [:str | str asSymbol]