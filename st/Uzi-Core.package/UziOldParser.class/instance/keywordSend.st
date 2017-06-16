grammar-keyword
keywordSend
	^ (binarySend , keywordMessage) token
		map: [:token || rcvr msg |
			rcvr := token parsedValue first.
			msg := token parsedValue second.
			self send: rcvr messages: { msg } token: token withoutTrailingBlanks]