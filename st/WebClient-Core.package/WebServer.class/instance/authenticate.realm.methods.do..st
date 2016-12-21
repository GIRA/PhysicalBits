authentication
authenticate: request realm: realm methods: accepted do: aBlock
	"Authenticates an incoming request using one of the accepted methods.

	Evaluates aBlock upon successful authentication. Responds with a 401
	(Unauthorized) if the authentication fails."

	| method resp |
	request headersAt: 'Authorization' do:[:authHeader|
		method := authHeader copyUpTo: Character space.
		(accepted anySatisfy:[:auth| auth sameAs: method]) ifTrue:[
			(self authAccept: method request: request realm: realm header: authHeader)
				ifTrue:[^aBlock value].
		].
	].

	"Send a 401 (unauthorized) response"
	resp := request newResponse protocol: 'HTTP/1.1' code: 401.
	resp headerAt: 'Content-Type' put: 'text/html; charset=utf-8'.
	accepted do:[:auth| | hdr |
		hdr := self authHeader: auth request: request realm: realm.
		hdr ifNotNil:[resp addHeader: 'WWW-Authenticate' value: hdr].
	].
	request sendResponse: resp content: '<html><head><title>401 Unauthorized</title></head><body><h1>401 Unauthorized</h1><p>You are not authorized to access the requested URL</p></body></html>'.
