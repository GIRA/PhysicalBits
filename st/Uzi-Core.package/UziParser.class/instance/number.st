grammar-literals
number
	^ ws, (float / integer) token , ws
	map: [:ign1 :token :ign2 | 
		(UziNumberNode value: token parsedValue) token: token]