accessing
serverString
	"Returns the server string to be used in responses"

	^'WebServer/1.5 (', 
		WebUtils mcVersion, '; ', 
		SystemVersion current version,'-', 
		SystemVersion current highestUpdate printString, '; ', 
		WebUtils platformName,')'.
