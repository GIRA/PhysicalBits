grammar-literals
number
	^ (float / integer) token 
	map: [:token | (UziNumberNode value: token parsedValue) token: token]