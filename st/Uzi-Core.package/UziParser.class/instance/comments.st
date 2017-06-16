grammar-separators
comments 
	^ ($" asParser ,  $" asParser negate star , $" asParser) plus flatten