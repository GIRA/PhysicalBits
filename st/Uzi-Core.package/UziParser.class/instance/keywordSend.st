grammar-keyword
keywordSend
	^ binarySend , keywordMessage map: [:rcvr :msg | self send: rcvr messages: { msg }]