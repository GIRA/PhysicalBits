grammar-scripts
script
	^ ws , ('task' asParser, ws , identifier , paramsList, scriptBody) token , ws
	map: [:ign1 :token :ign2 || nodes |
		nodes := token parsedValue.
		(UziScriptNode
			name: nodes third
			state: nil
			tickingRate: nil
			body: nodes fifth)
			token: token]