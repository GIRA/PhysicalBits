grammar-binary
binarySend
	^ (unarySend , binaryTail optional) token
		map: [:token || rcvr msgs |
			rcvr := token parsedValue first.
			msgs := token parsedValue second.
			(self send: rcvr messages: msgs) token: token withoutTrailingBlanks]