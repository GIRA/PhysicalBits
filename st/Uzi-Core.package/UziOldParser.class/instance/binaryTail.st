grammar-binary
binaryTail
	^ binaryMessage , ws , binaryTail optional
		map: [:msg :ign :tail | self flatten: msg tail: tail]