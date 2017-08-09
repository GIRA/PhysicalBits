grammar-scripts
function
	^ ws , ('func' asParser, ws , identifier , paramsList,  block) token , ws
	map: [:ign1 :token :ign2 || nodes |
		nodes := token parsedValue.
		(UziScriptNode
			name: nodes third
			arguments: nodes fourth
			state: nil
			tickingRate: nil
			body: nodes fifth)
			token: token]