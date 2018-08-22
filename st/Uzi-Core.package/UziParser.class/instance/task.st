grammar-scripts
task
	^ ws , ('task' asParser, ws , identifier , paramsList, taskState , tickingRate optional , block) token , ws
	map: [:ign1 :token :ign2 || nodes |
		nodes := token parsedValue.
		(UziTaskNode
			name: nodes third
			arguments: nodes fourth
			state: (nodes fifth ifNil: [#once])
			tickingRate: nodes sixth 
			body: nodes seventh)
			token: token]