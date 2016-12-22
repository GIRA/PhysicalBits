grammar-unary
unarySend
	^ operand , ws , unaryTail optional
		map: [:rcvr :ign :msgs | self send: rcvr messages: msgs]