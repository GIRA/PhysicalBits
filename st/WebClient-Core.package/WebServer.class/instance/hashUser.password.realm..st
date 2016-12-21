authentication
hashUser: user password: pass realm: realm
	"Creates a hash for the given username password pair.
	This method is the ha1 part of digest auth and can be used for both digest
	as well as basic auth."

	^WebUtils md5Digest: user, ':', realm, ':', pass