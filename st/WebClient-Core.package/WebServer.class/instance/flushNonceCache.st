authentication
flushNonceCache
	"Flushes expired nonces from the cache"

	| deadline |
	deadline := Time totalSeconds.
	mutex critical:[
		nonceCache := nonceCache reject:[:nonceData| deadline > nonceData second]
	].
