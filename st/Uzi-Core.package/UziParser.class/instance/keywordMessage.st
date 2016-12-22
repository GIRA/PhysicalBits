grammar-keyword
keywordMessage
	^ (ws , ((keywordPair , ws) map: [:pair :ign | pair]) plus)
		map: [:ign :pairs || selector args |
			selector := pairs inject: '' into: [:subTotal :next | subTotal , next key].
			args := pairs collect: [:pair | pair value].
			self message: selector args: args]