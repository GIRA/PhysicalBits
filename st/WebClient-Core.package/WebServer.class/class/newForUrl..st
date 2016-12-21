registry
newForUrl: urlString
	"Create a new WebServer instance registered for a given URL string."

	| server |
	"Out with the old ...."
	server := self removeUrl: urlString.
	server ifNotNil:[server destroy].

	"... and in with the new."
	server := self new.
	server siteUrl: urlString.
	self registry at: urlString put: server.
	^server
