grammar-scripts
procedure
	^ ws , ('proc' asParser, ws , identifier , paramsList,  block) token , ws
	map: [:ign1 :token :ign2 || nodes |
		nodes := token parsedValue.
		self flag: #Richo. "Verify that no return value is present in the body?"
		(UziScriptNode
			name: nodes third
			arguments: nodes fourth
			state: nil
			tickingRate: nil
			body: nodes fifth)
			token: token]