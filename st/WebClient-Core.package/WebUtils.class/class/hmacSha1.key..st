oauth
hmacSha1: message key: signKey
	"Compute the SHA1 HMAC for the given message"

	| blockSize key ipad opad |
	blockSize := 64. "SHA1 block size"
	key := signKey asByteArray.
	key size > blockSize ifTrue:[key := self sha1Hash: key].
	key size < blockSize ifTrue:[key := key, (ByteArray new: blockSize - key size)].
	ipad := ByteArray new: blockSize withAll: 16r36.
	opad := ByteArray new: blockSize withAll: 16r5c.
	^self sha1Hash: 
		(key with: opad collect:[:b1 :b2| b1 bitXor: b2]), 
		(self sha1Hash: (key with: ipad collect:[:b1 :b2| b1 bitXor: b2]), message)