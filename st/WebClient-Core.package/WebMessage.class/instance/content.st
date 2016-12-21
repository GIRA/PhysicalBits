accessing
content
	"Reads and caches available content and returns it."

	^content ifNil:[content := self getContent].
