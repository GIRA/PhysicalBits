authentication
newNonce
	"Create a new nonce to be used for a client authentication session"

	| nonce |
	nonce := UUID new hex.
	mutex critical:[
		nonceCache at: nonce put: {''. Time totalSeconds + self nonceDeadline}.
	].
	^nonce