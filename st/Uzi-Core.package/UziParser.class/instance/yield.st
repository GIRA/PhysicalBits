grammar-statements
yield
	^ ws, ('yield' asParser , ws , $; asParser) token, ws
		map: [:ign1 :token :ign2 | UziYieldNode new token: token]