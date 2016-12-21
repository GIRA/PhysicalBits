authentication
digestAuth: request realm: realm header: authHeader
	"Authenticates an incoming request using Digest auth."

	| user nonce uri response ha1 ha2 md5 qop nc cnonce 
	  nonceData nonceCounter nonceTimeout params |

	params := WebUtils parseAuthParams: authHeader.

	"Flush the nonce cache randomly about every 100 auth attempts"
	100 atRandom = 42 ifTrue:[self flushNonceCache].

	user := params at: 'username' ifAbsent:[''].
	nonce := params at: 'nonce' ifAbsent:[''].
	uri := params at: 'uri' ifAbsent:[request url].
	response := params at: 'response' ifAbsent:[''].
	
	qop := params at: 'qop' ifAbsent:[''].
	qop = 'auth' ifFalse:[^false]. "we require qop=auth"

	nc := params at: 'nc' ifAbsent:[''].
	cnonce := params at: 'cnonce' ifAbsent:[''].
	
	"Verify that this is a nonce that we have handed out, that it isn't expired
	and that the nc counter has increased from the last use."
	mutex critical:[
		nonceData := nonceCache at: nonce ifAbsent:[^false].
	].
	nonceCounter := nonceData first.		"last used nc"
	nonceTimeout := nonceData second.	"timeout for nonce"
	nc <= nonceCounter ifTrue:[^false]. 	"nc must increase"
	Time totalSeconds > nonceTimeout ifTrue:[^false]. "nonce expired"
	nonceData at: 1 put: nc.

	ha1 := self passwordHashAt: user, ':', realm.
	ha2 := WebUtils md5Digest: request method, ':', uri.
	md5 := WebUtils md5Digest: ha1, ':', nonce, ':', nc, ':', cnonce, ':', qop, ':', ha2.
	^md5 = response
