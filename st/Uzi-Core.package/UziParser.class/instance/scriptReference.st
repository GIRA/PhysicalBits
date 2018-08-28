grammar-script references
scriptReference
	^ (ws , identifier , ws) map: [:ign1 :name :ign2 | name]