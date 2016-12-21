sending
writeCookiesFor: request on: stream
	"Write the cookies for a particular request"

	(cookies select:[:ck| (request acceptsCookie: ck from: self )]) ifNotEmpty:[:ckset|
		stream nextPutAll: 'Cookie: '.
		ckset 
			do:[:ck| stream nextPutAll: ck name,'=', ck value]
			separatedBy:[stream  nextPutAll:'; '].
		stream crlf.
	].
