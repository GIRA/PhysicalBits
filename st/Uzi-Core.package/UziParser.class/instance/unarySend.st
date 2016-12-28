grammar-unary
unarySend
	^ (operand , ws , unaryTail optional) token
		map: [:token || rcvr msgs | 
			rcvr := token parsedValue first.
			msgs := token parsedValue last.
			(self send: rcvr messages: msgs) token: token]