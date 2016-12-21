authentication
basicAuth: request realm: realm header: authHeader
	"Perform Basic auth for the given request"

	| data user pass hash |
	data := (authHeader copyAfter: $ ) base64Decoded.
	user := data copyUpTo: $:.
	pass := data copyAfter: $:.
	data atAllPut: $*. data := nil.

	hash := self hashUser: user password: pass realm: realm.
	pass atAllPut: $*. pass := nil.

	^(self passwordHashAt: user, ':', realm) = hash
