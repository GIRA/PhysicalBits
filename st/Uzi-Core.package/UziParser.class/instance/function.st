grammar-scripts
function
	^ ws , ('func' asParser, ws , identifier , paramsList,  block) token , ws
	map: [:ign1 :token :ign2 || nodes |
		nodes := token parsedValue.
		self flag: #Richo. "Verify that a return value is present in the body?"
		(UziFunctionNode
			name: nodes third
			arguments: nodes fourth
			body: nodes fifth)
			token: token]