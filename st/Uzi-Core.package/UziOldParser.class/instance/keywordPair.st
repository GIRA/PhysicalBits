grammar-keyword
keywordPair
	^ keywordSelector , ws , binarySend , ws
		map: [:key :ign1 :arg :ign2 | key -> arg]