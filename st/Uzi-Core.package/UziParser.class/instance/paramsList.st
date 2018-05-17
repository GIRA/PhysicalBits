grammar-scripts
paramsList
	^ ws, 
	($( asParser , ws , 
		(argument separatedBy: (ws , $, asParser , ws)) optional, 
		ws , $) asParser) token, 
	ws
	map: [:ign1 :token :ign2 |
		token parsedValue third
			ifNil: #() 
			ifNotNil: [:args | (1 to: args size by: 2) collect: [:i | args at: i]]]