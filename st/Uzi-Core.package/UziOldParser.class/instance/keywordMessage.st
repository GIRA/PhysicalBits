grammar-keyword
keywordMessage
	^ (ws , ((keywordPair , ws) map: [:pair :ign | pair]) plus token)
		map: [:ign :token || pairs selector args |
			pairs := token parsedValue.
			selector := pairs inject: '' into: [:subTotal :next | subTotal , next key].
			args := pairs collect: [:pair | pair value].
			self message: selector args: args token: token]