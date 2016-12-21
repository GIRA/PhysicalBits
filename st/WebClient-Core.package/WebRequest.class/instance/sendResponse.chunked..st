responses
sendResponse: code chunked: chunkBlock
	"Send a chunked response"

	^self sendResponse: code chunked: chunkBlock do:[:resp]