initialize
defaultUserAgent
	"Answer the default User-Agent string to use for WebClient"

	^'WebClient/1.5 (', 
		WebUtils mcVersion, '; ', 
		SystemVersion current version,'-', 
		SystemVersion current highestUpdate printString, '; ', 
		WebUtils platformName,')'.
