accessing
url
	"The url without fragment or arguments"

	^((rawUrl copyUpTo: $#) copyUpTo: $?) unescapePercentsWithTextEncoding: nil