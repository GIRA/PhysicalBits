cookies
setCookiesDo: aBlock
	"Evaluate aBlock with all the cookies deined in the request"

	self headersAt: 'Set-Cookie' do:[:hdr|
		aBlock value: (WebCookie new readFrom: hdr readStream)
	].
