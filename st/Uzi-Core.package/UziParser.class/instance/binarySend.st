grammar-binary
binarySend
	^ unarySend , binaryTail optional
		map: [:rcvr :msgs | self send: rcvr messages: msgs]