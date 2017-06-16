grammar-unary
unaryTail
	^ unaryMessage , ws , unaryTail optional , ws
		map: [:msg :ign1 :tail :ign2 | self flatten: msg tail: tail]